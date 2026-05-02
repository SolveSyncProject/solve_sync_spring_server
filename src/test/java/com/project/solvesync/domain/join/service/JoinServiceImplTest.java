package com.project.solvesync.domain.join.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.invitation.entity.InvitationStatus;
import com.project.solvesync.domain.invitation.repository.InvitationRepository;
import com.project.solvesync.domain.join.dto.JoinDtos;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;
import com.project.solvesync.domain.join.repository.JoinRequestRepository;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.PeriodUnit;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import com.project.solvesync.domain.room.entity.RoomRule;
import com.project.solvesync.domain.room.entity.RoomRulePlatform;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoinServiceImplTest {

    @Mock
    private StudyRoomRepository roomRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private RoomMembershipRepository membershipRepository;

    @Mock
    private UserPlatformAccountRepository userPlatformAccountRepository;

    @Test
    void requestJoin_rejectsWhenPendingInvitationExists() {
        JoinServiceImpl service = new JoinServiceImpl(
                roomRepository,
                joinRequestRepository,
                invitationRepository,
                membershipRepository,
                userPlatformAccountRepository
        );

        Long userId = 2L;
        Long roomId = 1L;
        StudyRoom room = createDraftRoom();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(membershipRepository.existsByRoom_IdAndUserId(roomId, userId)).thenReturn(false);
        when(joinRequestRepository.existsByRoom_IdAndUserIdAndStatus(roomId, userId, JoinRequestStatus.PENDING)).thenReturn(false);
        when(invitationRepository.existsByRoom_IdAndInviteeUserIdAndStatus(roomId, userId, InvitationStatus.PENDING)).thenReturn(true);

        BaseException exception = assertThrows(
                BaseException.class,
                () -> service.requestJoin(userId, roomId, new JoinDtos.CreateRequest("hi", null, List.of(Platform.BOJ)))
        );

        assertEquals(BaseResponseStatus.JOIN_REQUEST_CONFLICT_INVITATION, exception.getStatus());
        verify(joinRequestRepository, never()).save(any());
    }

    private StudyRoom createDraftRoom() {
        StudyRoom room = StudyRoom.create(
                1L,
                "room",
                RoomVisibility.PUBLIC,
                true,
                "INVITECODE",
                "Asia/Seoul",
                OffsetDateTime.parse("2026-04-30T09:00:00+09:00")
        );
        room.attachRule(RoomRule.create(PeriodUnit.DAILY, 3, false));
        room.addRulePlatform(RoomRulePlatform.create(Platform.BOJ, null, null));
        room.addRulePlatform(RoomRulePlatform.create(Platform.CODEFORCES, null, null));
        return room;
    }
}
