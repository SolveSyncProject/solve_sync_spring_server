package com.project.solvesync.domain.invitation.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.invitation.dto.InvitationDtos;
import com.project.solvesync.domain.invitation.entity.Invitation;
import com.project.solvesync.domain.invitation.entity.InvitationStatus;
import com.project.solvesync.domain.invitation.repository.InvitationRepository;
import com.project.solvesync.domain.join.repository.JoinRequestRepository;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.PeriodUnit;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import com.project.solvesync.domain.room.entity.RoomRule;
import com.project.solvesync.domain.room.entity.RoomRulePlatform;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock
    private StudyRoomRepository roomRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private RoomMembershipRepository membershipRepository;

    @Mock
    private UserPlatformAccountRepository userPlatformAccountRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private UserService userService;

    @Test
    void accept_createsMembershipWithSelectedPlatforms() {
        InvitationServiceImpl service = new InvitationServiceImpl(
                roomRepository,
                invitationRepository,
                membershipRepository,
                userPlatformAccountRepository,
                joinRequestRepository,
                userService
        );

        Long invitationId = 100L;
        Long inviteeId = 20L;
        StudyRoom room = createDraftRoom();
        Invitation invitation = Invitation.create(room, 1L, inviteeId, "welcome");

        when(invitationRepository.findWithRoomById(invitationId)).thenReturn(Optional.of(invitation));
        when(membershipRepository.existsByRoom_IdAndUserId(room.getId(), inviteeId)).thenReturn(false);
        when(userPlatformAccountRepository.existsByUserIdAndPlatform(inviteeId, Platform.BOJ)).thenReturn(true);
        when(userPlatformAccountRepository.existsByUserIdAndPlatform(inviteeId, Platform.CODEFORCES)).thenReturn(true);
        when(userPlatformAccountRepository.findByUserIdAndPlatform(inviteeId, Platform.BOJ))
                .thenReturn(Optional.of(UserPlatformAccount.create(inviteeId, Platform.BOJ, "boj-user")));
        when(userPlatformAccountRepository.findByUserIdAndPlatform(inviteeId, Platform.CODEFORCES))
                .thenReturn(Optional.of(UserPlatformAccount.create(inviteeId, Platform.CODEFORCES, "cf-user")));
        when(membershipRepository.save(any(RoomMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvitationDtos.AcceptResponse response = service.accept(
                inviteeId,
                invitationId,
                new InvitationDtos.AcceptRequest(List.of(Platform.BOJ, Platform.CODEFORCES))
        );

        ArgumentCaptor<RoomMembership> membershipCaptor = ArgumentCaptor.forClass(RoomMembership.class);
        verify(membershipRepository).save(membershipCaptor.capture());

        RoomMembership membership = membershipCaptor.getValue();
        assertNull(response.membershipId());
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(inviteeId, membership.getUserId());
        assertEquals(2, membership.getPlatforms().size());
        assertEquals("boj-user", membership.getPlatforms().get(0).getHandleSnapshot());
        assertEquals("cf-user", membership.getPlatforms().get(1).getHandleSnapshot());
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
