package com.project.solvesync.domain.invitation.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.invitation.dto.InvitationDtos;
import com.project.solvesync.domain.invitation.entity.Invitation;
import com.project.solvesync.domain.invitation.entity.InvitationStatus;
import com.project.solvesync.domain.invitation.repository.InvitationRepository;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;
import com.project.solvesync.domain.join.repository.JoinRequestRepository;
import com.project.solvesync.domain.membership.entity.ParticipationPlatform;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.domain.user.service.UserService;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final StudyRoomRepository roomRepository;
    private final InvitationRepository invitationRepository;
    private final RoomMembershipRepository membershipRepository;
    private final UserPlatformAccountRepository userPlatformAccountRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final UserService userService;

    @Override
    public void invite(Long actorId, Long roomId, InvitationDtos.CreateRequest request) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        // MVP 정책: 모집(DRAFT) 상태에서만 초대
        if (room.getStatus() != RoomStatus.DRAFT) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_DRAFT);
        }

        // 방장만 초대 가능 (MVP: room.ownerId로 체크)
        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }

        Long inviteeUserId = userService.getByUsernameOrThrow(request.inviteeUsername()).getId();

        if (Objects.equals(inviteeUserId, actorId)) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST, "자기 자신을 초대할 수 없습니다.");
        }

        // 이미 멤버면 초대 불가
        if (membershipRepository.existsByRoom_IdAndUserId(roomId, inviteeUserId)) {
            throw new BaseException(BaseResponseStatus.MEMBERSHIP_ALREADY_EXISTS);
        }

        // 이미 PENDING 초대가 있으면 불가
        if (invitationRepository.existsByRoom_IdAndInviteeUserIdAndStatus(roomId, inviteeUserId, InvitationStatus.PENDING)) {
            throw new BaseException(BaseResponseStatus.INVITATION_ALREADY_EXISTS);
        }

        // PENDING 참여 신청이 있으면 초대 불가 (정책)
        if (joinRequestRepository.existsByRoom_IdAndUserIdAndStatus(roomId, inviteeUserId, JoinRequestStatus.PENDING)) {
            throw new BaseException(BaseResponseStatus.JOIN_REQUEST_ALREADY_EXISTS, "이미 참여 신청(PENDING)이 존재합니다.");
        }

        Invitation inv = Invitation.create(room, actorId, inviteeUserId, request.message());
        invitationRepository.save(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationDtos.InvitationItem> listRoomInvitations(Long actorId, Long roomId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }

        return invitationRepository.findAllByRoom_IdOrderByIdDesc(roomId).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationDtos.InvitationItem> listMyInvitations(Long userId) {
        return invitationRepository.findAllByInviteeUserIdOrderByIdDesc(userId).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public InvitationDtos.AcceptResponse accept(Long userId, Long invitationId, InvitationDtos.AcceptRequest request) {
        Invitation inv = invitationRepository.findWithRoomById(invitationId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVITATION_NOT_FOUND));

        if (!Objects.equals(inv.getInviteeUserId(), userId)) {
            throw new BaseException(BaseResponseStatus.INVITATION_FORBIDDEN);
        }

        if (!inv.isPending()) {
            throw new BaseException(BaseResponseStatus.INVITATION_NOT_PENDING);
        }

        StudyRoom room = inv.getRoom();

        // MVP 정책: 모집(DRAFT)에서만 수락 가능
        if (room.getStatus() != RoomStatus.DRAFT) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_DRAFT);
        }

        // 이미 멤버면 수락 불가
        if (membershipRepository.existsByRoom_IdAndUserId(room.getId(), userId)) {
            throw new BaseException(BaseResponseStatus.MEMBERSHIP_ALREADY_EXISTS);
        }

        Set<Platform> selected = normalizePlatforms(request.platforms());
        validatePlatformSelection(room, selected);
        validateUserHasPlatformAccounts(userId, selected);

        List<ParticipationPlatform> participationPlatforms = selected.stream()
                .map(p -> toParticipationPlatform(userId, p))
                .toList();

        RoomMembership membership = RoomMembership.member(room, userId, participationPlatforms);
        membershipRepository.save(membership);

        inv.accept();
        return new InvitationDtos.AcceptResponse(membership.getId());
    }

    @Override
    public void decline(Long userId, Long invitationId) {
        Invitation inv = invitationRepository.findWithRoomById(invitationId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVITATION_NOT_FOUND));

        if (!Objects.equals(inv.getInviteeUserId(), userId)) {
            throw new BaseException(BaseResponseStatus.INVITATION_FORBIDDEN);
        }
        if (!inv.isPending()) {
            throw new BaseException(BaseResponseStatus.INVITATION_NOT_PENDING);
        }

        inv.decline();
    }

    @Override
    public void cancel(Long actorId, Long invitationId) {
        Invitation inv = invitationRepository.findWithRoomById(invitationId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVITATION_NOT_FOUND));

        StudyRoom room = inv.getRoom();

        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }
        if (!inv.isPending()) {
            throw new BaseException(BaseResponseStatus.INVITATION_NOT_PENDING);
        }

        inv.cancel();
    }

    private InvitationDtos.InvitationItem toItem(Invitation inv) {
        return new InvitationDtos.InvitationItem(
                inv.getId(),
                inv.getRoom().getId(),
                inv.getInviterUserId(),
                inv.getInviteeUserId(),
                inv.getMessage(),
                inv.getStatus()
        );
    }

    private void validatePlatformSelection(StudyRoom room, Set<Platform> selected) {
        Set<Platform> allowed = room.getRulePlatforms().stream()
                .map(rp -> rp.getPlatform())
                .collect(Collectors.toSet());

        if (!allowed.containsAll(selected)) {
            throw new BaseException(BaseResponseStatus.INVALID_PLATFORM_SELECTION);
        }
    }

    private void validateUserHasPlatformAccounts(Long userId, Set<Platform> selected) {
        List<Platform> missing = new ArrayList<>();
        for (Platform p : selected) {
            if (!userPlatformAccountRepository.existsByUserIdAndPlatform(userId, p)) {
                missing.add(p);
            }
        }
        if (!missing.isEmpty()) {
            throw new BaseException(
                    BaseResponseStatus.PLATFORM_ACCOUNT_REQUIRED,
                    "선택한 플랫폼 계정을 먼저 등록해주세요. missing=" + missing
            );
        }
    }

    private ParticipationPlatform toParticipationPlatform(Long userId, Platform platform) {
        UserPlatformAccount account = userPlatformAccountRepository.findByUserIdAndPlatform(userId, platform)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PLATFORM_ACCOUNT_REQUIRED));

        // (platform, accountId, handleSnapshot)
        return new ParticipationPlatform(platform, account.getId(), account.getHandle());
    }

    private Set<Platform> normalizePlatforms(List<Platform> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "platforms는 최소 1개 이상 필요합니다.");
        }
        return new LinkedHashSet<>(platforms);
    }
}
