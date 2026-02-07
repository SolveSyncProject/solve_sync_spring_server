package com.project.solvesync.domain.solve.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.solve.dto.DailySummaryDtos;
import com.project.solvesync.domain.solve.entity.DailySolveDifficultyStat;
import com.project.solvesync.domain.solve.entity.DailySolveSummary;
import com.project.solvesync.domain.solve.mq.DailySummaryMessagePublisher;
import com.project.solvesync.domain.solve.mq.DailySummaryUpsertMessage;
import com.project.solvesync.domain.solve.repository.DailySolveSummaryRepository;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailySummaryServiceImpl implements DailySummaryService {

    private final DailySolveSummaryRepository dailySolveSummaryRepository;
    private final UserPlatformAccountRepository userPlatformAccountRepository;
    private final DailySummaryMessagePublisher publisher;

    @Value("${solvesync.solve.http.max-batch-size:5000}")
    private int maxHttpBatchSize;

    @Override
    public int publishBatch(DailySummaryDtos.UpsertBatchRequest request) {
        List<DailySummaryDtos.UpsertItem> items = request.items();
        if (items.size() > maxHttpBatchSize) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR,
                    "items 개수가 너무 큽니다. max=" + maxHttpBatchSize + ", got=" + items.size());
        }

        int accepted = 0;
        for (DailySummaryDtos.UpsertItem item : items) {
            DailySummaryUpsertMessage msg = DailySummaryUpsertMessage.from(item);
            publisher.publishUpsert(msg);
            accepted++;
        }
        return accepted;
    }

    @Override
    @Transactional
    public void upsertFromMessage(DailySummaryUpsertMessage message) {
        // 1) 플랫폼 계정 존재 + platform 스냅샷 확보
        UserPlatformAccount acc = userPlatformAccountRepository.findById(message.userPlatformAccountId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PLATFORM_ACCOUNT_REQUIRED,
                        "플랫폼 계정이 존재하지 않습니다. userPlatformAccountId=" + message.userPlatformAccountId()));

        Platform platform = acc.getPlatform();

        // 2) upsert key: (userPlatformAccountId, date)
        DailySolveSummary summary = dailySolveSummaryRepository
                .findByUserPlatformAccountIdAndDate(message.userPlatformAccountId(), message.date())
                .orElseGet(() -> DailySolveSummary.create(message.userPlatformAccountId(), platform, message.date()));

        // 3) difficultyStats -> entity list (중복 난이도는 merge)
        List<DailySolveDifficultyStat> stats = buildStats(message.difficultyStats());

        int solvedCount = (message.solvedCount() != null)
                ? message.solvedCount()
                : stats.stream().mapToInt(DailySolveDifficultyStat::getSolvedCount).sum();

        summary.applyUpsert(solvedCount, stats, message.generatedAt());
        dailySolveSummaryRepository.save(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySummaryDtos.SummaryView> getSummaries(Long userPlatformAccountId, LocalDate from, LocalDate to) {
        List<DailySolveSummary> list = dailySolveSummaryRepository
                .findAllByUserPlatformAccountIdAndDateBetweenOrderByDateAsc(userPlatformAccountId, from, to);

        return list.stream().map(s -> new DailySummaryDtos.SummaryView(
                s.getUserPlatformAccountId(),
                s.getPlatform(),
                s.getDate(),
                s.getSolvedCount(),
                s.getGeneratedAt(),
                s.getDifficultyStats().stream()
                        .map(d -> new DailySummaryDtos.DifficultyCount(d.getDifficulty(), d.getSolvedCount()))
                        .toList()
        )).toList();
    }

    private List<DailySolveDifficultyStat> buildStats(List<DailySummaryUpsertMessage.DifficultyCount> list) {
        if (list == null || list.isEmpty()) return List.of();

        Map<Integer, Integer> merged = new HashMap<>();
        for (DailySummaryUpsertMessage.DifficultyCount d : list) {
            if (d.difficulty() < 0 || d.solvedCount() < 0) {
                throw new BaseException(BaseResponseStatus.VALIDATION_ERROR,
                        "difficultyStats의 difficulty/solvedCount는 0 이상이어야 합니다.");
            }
            merged.merge(d.difficulty(), d.solvedCount(), Integer::sum);
        }

        return merged.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> DailySolveDifficultyStat.of(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
