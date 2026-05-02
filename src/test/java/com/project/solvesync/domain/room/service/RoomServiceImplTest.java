package com.project.solvesync.domain.room.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.room.dto.RoomDtos;
import com.project.solvesync.domain.room.entity.PeriodUnit;
import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.user.entity.User;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.domain.user.repository.UserRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private StudyRoomRepository roomRepository;

    @Mock
    private UserPlatformAccountRepository userPlatformAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createRoom_savesOwnerMembershipAndRulePlatforms() {
        RoomServiceImpl service = new RoomServiceImpl(roomRepository, userPlatformAccountRepository, userRepository);

        Long ownerId = 1L;
        RoomDtos.CreateRequest request = new RoomDtos.CreateRequest(
                " SolveSync Room ",
                RoomVisibility.PUBLIC,
                true,
                "Asia/Seoul",
                OffsetDateTime.parse("2026-04-30T09:00:00+09:00"),
                new RoomDtos.Rule(PeriodUnit.DAILY, 3, false),
                List.of(
                        new RoomDtos.RulePlatform(Platform.BOJ, 10, 20),
                        new RoomDtos.RulePlatform(Platform.CODEFORCES, null, null)
                ),
                List.of(Platform.BOJ)
        );

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(User.create("owner@example.com", "owner")));
        when(userPlatformAccountRepository.findByUserIdAndPlatform(ownerId, Platform.BOJ))
                .thenReturn(Optional.of(UserPlatformAccount.create(ownerId, Platform.BOJ, " wlals123 ")));
        when(roomRepository.save(any(StudyRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomDtos.CreateResponse response = service.createRoom(ownerId, request);

        ArgumentCaptor<StudyRoom> roomCaptor = ArgumentCaptor.forClass(StudyRoom.class);
        verify(roomRepository).save(roomCaptor.capture());

        StudyRoom savedRoom = roomCaptor.getValue();
        assertEquals(RoomStatus.DRAFT, response.status());
        assertEquals("SolveSync Room", savedRoom.getName());
        assertNotNull(savedRoom.getInviteCode());
        assertEquals(2, savedRoom.getRulePlatforms().size());
        assertNotNull(savedRoom.getRule());
        assertEquals(1, savedRoom.getMemberships().size());
        assertEquals(ownerId, savedRoom.getMemberships().get(0).getUserId());
        assertEquals(Platform.BOJ, savedRoom.getMemberships().get(0).getPlatforms().get(0).getPlatform());
        assertEquals("wlals123", savedRoom.getMemberships().get(0).getPlatforms().get(0).getHandleSnapshot());
    }

    @Test
    void createRoom_rejectsOwnerPlatformOutsideRulePlatforms() {
        RoomServiceImpl service = new RoomServiceImpl(roomRepository, userPlatformAccountRepository, userRepository);

        Long ownerId = 1L;
        RoomDtos.CreateRequest request = new RoomDtos.CreateRequest(
                "room",
                RoomVisibility.PUBLIC,
                true,
                "Asia/Seoul",
                OffsetDateTime.parse("2026-04-30T09:00:00+09:00"),
                new RoomDtos.Rule(PeriodUnit.DAILY, 1, false),
                List.of(new RoomDtos.RulePlatform(Platform.BOJ, null, null)),
                List.of(Platform.BOJ, Platform.CODEFORCES)
        );

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(User.create("owner@example.com", "owner")));
        when(userPlatformAccountRepository.findByUserIdAndPlatform(ownerId, Platform.BOJ))
                .thenReturn(Optional.of(UserPlatformAccount.create(ownerId, Platform.BOJ, "boj-user")));
        when(userPlatformAccountRepository.findByUserIdAndPlatform(ownerId, Platform.CODEFORCES))
                .thenReturn(Optional.of(UserPlatformAccount.create(ownerId, Platform.CODEFORCES, "cf-user")));

        BaseException exception = assertThrows(BaseException.class, () -> service.createRoom(ownerId, request));

        assertEquals(BaseResponseStatus.INVALID_PLATFORM_SELECTION, exception.getStatus());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void activateRoom_setsEvaluationStartAtToNextMidnightInRoomTimezone() {
        RoomServiceImpl service = new RoomServiceImpl(roomRepository, userPlatformAccountRepository, userRepository);

        Long roomId = 10L;
        Long ownerId = 1L;
        StudyRoom room = StudyRoom.create(
                ownerId,
                "room",
                RoomVisibility.PUBLIC,
                true,
                "INVITECODE",
                "Asia/Seoul",
                OffsetDateTime.parse("2026-04-30T09:00:00+09:00")
        );

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        RoomDtos.ActivateResponse response = service.activateRoom(ownerId, roomId);

        assertEquals(RoomStatus.ACTIVE, response.status());
        assertNotNull(response.activatedAt());
        assertNotNull(response.evaluationStartAt());

        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate activatedLocalDate = response.activatedAt().atZoneSameInstant(zoneId).toLocalDate();
        OffsetDateTime expected = activatedLocalDate.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        assertEquals(expected, response.evaluationStartAt());
    }
}
