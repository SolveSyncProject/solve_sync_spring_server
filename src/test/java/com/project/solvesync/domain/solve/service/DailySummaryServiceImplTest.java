package com.project.solvesync.domain.solve.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.solve.dto.DailySummaryDtos;
import com.project.solvesync.domain.solve.entity.DailySolveSummary;
import com.project.solvesync.domain.solve.mq.DailySummaryMessagePublisher;
import com.project.solvesync.domain.solve.mq.DailySummaryUpsertMessage;
import com.project.solvesync.domain.solve.repository.DailySolveSummaryRepository;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailySummaryServiceImplTest {

    @Mock
    private DailySolveSummaryRepository dailySolveSummaryRepository;

    @Mock
    private UserPlatformAccountRepository userPlatformAccountRepository;

    @Mock
    private DailySummaryMessagePublisher publisher;

    @Test
    void publishBatch_rejectsWhenBatchExceedsDefaultLimit() {
        DailySummaryServiceImpl service = new DailySummaryServiceImpl(
                dailySolveSummaryRepository,
                userPlatformAccountRepository,
                publisher
        );

        List<DailySummaryDtos.UpsertItem> items = IntStream.range(0, 5001)
                .mapToObj(i -> new DailySummaryDtos.UpsertItem(
                        1L,
                        LocalDate.parse("2026-04-30"),
                        1,
                        null,
                        null
                ))
                .toList();

        BaseException exception = assertThrows(
                BaseException.class,
                () -> service.publishBatch(new DailySummaryDtos.UpsertBatchRequest(items))
        );

        assertEquals(BaseResponseStatus.VALIDATION_ERROR, exception.getStatus());
    }

    @Test
    void upsertFromMessage_mergesDuplicateDifficultyStatsAndDerivesSolvedCount() {
        DailySummaryServiceImpl service = new DailySummaryServiceImpl(
                dailySolveSummaryRepository,
                userPlatformAccountRepository,
                publisher
        );

        Long accountId = 10L;
        LocalDate date = LocalDate.parse("2026-04-30");
        OffsetDateTime generatedAt = OffsetDateTime.parse("2026-04-30T10:15:30+09:00");
        DailySummaryUpsertMessage message = new DailySummaryUpsertMessage(
                accountId,
                date,
                null,
                List.of(
                        new DailySummaryUpsertMessage.DifficultyCount(1, 2),
                        new DailySummaryUpsertMessage.DifficultyCount(1, 3),
                        new DailySummaryUpsertMessage.DifficultyCount(5, 1)
                ),
                generatedAt
        );

        when(userPlatformAccountRepository.findById(accountId))
                .thenReturn(Optional.of(UserPlatformAccount.create(1L, Platform.BOJ, "boj-user")));
        when(dailySolveSummaryRepository.findByUserPlatformAccountIdAndDate(accountId, date))
                .thenReturn(Optional.empty());
        when(dailySolveSummaryRepository.save(any(DailySolveSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.upsertFromMessage(message);

        ArgumentCaptor<DailySolveSummary> summaryCaptor = ArgumentCaptor.forClass(DailySolveSummary.class);
        verify(dailySolveSummaryRepository).save(summaryCaptor.capture());

        DailySolveSummary saved = summaryCaptor.getValue();
        assertEquals(Platform.BOJ, saved.getPlatform());
        assertEquals(6, saved.getSolvedCount());
        assertEquals(generatedAt, saved.getGeneratedAt());
        assertEquals(2, saved.getDifficultyStats().size());
        assertEquals(1, saved.getDifficultyStats().get(0).getDifficulty());
        assertEquals(5, saved.getDifficultyStats().get(0).getSolvedCount());
        assertEquals(5, saved.getDifficultyStats().get(1).getDifficulty());
        assertEquals(1, saved.getDifficultyStats().get(1).getSolvedCount());
    }
}
