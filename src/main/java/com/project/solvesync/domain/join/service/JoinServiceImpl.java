package com.project.solvesync.domain.join.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.join.dto.JoinDtos;
import com.project.solvesync.domain.join.entity.JoinRequest;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;
import com.project.solvesync.domain.join.repository.JoinRequestRepository;
import com.project.solvesync.domain.membership.entity.ParticipationPlatform;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
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
public class JoinServiceImpl implements JoinService {

    private final StudyRoomRepository roomRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final RoomMembershipRepository membershipRepository;
    private final UserPlatformAccountRepository userPlatformAccountRepository;

    @Override
    public void requestJoin(Long userId, Long roomId, JoinDtos.CreateRequest req) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        // MVP: 모집(DRAFT) 상태에서만 신청 가능
        if (room.getStatus() != RoomStatus.DRAFT) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_DRAFT);
        }

        validateInviteOrListing(room, req.inviteCode());

        // 이미 멤버면 신청 불가
        if (membershipRepository.existsByRoom_IdAndUserId(roomId, userId)) {
            throw new BaseException(BaseResponseStatus.MEMBERSHIP_ALREADY_EXISTS);
        }

        // PENDING 중복 신청 방지
        if (joinRequestRepository.existsByRoom_IdAndUserIdAndStatus(roomId, userId, JoinRequestStatus.PENDING)) {
            throw new BaseException(BaseResponseStatus.JOIN_REQUEST_ALREADY_EXISTS);
        }

        Set<Platform> selected = normalizePlatforms(req.platforms());
        validatePlatformSelection(room, selected);
        validateUserHasPlatformAccounts(userId, selected);

        JoinRequest joinRequest = JoinRequest.create(room, userId, req.introText(), selected);
        joinRequestRepository.save(joinRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinDtos.JoinRequestItem> listJoinRequests(Long actorId, Long roomId, JoinRequestStatus status) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        requireOwner(actorId, room);

        List<JoinRequest> requests = (status == null)
                ? joinRequestRepository.findAllByRoom_IdOrderByIdDesc(roomId)
                : joinRequestRepository.findAllByRoom_IdAndStatusOrderByIdDesc(roomId, status);

        return requests.stream()
                .map(jr -> new JoinDtos.JoinRequestItem(
                        jr.getId(),
                        jr.getUserId(),
                        jr.getIntroText(),
                        jr.getStatus(),
                        new ArrayList<>(jr.getPlatforms())
                ))
                .toList();
    }

    @Override
    public JoinDtos.ApproveResponse approve(Long actorId, Long joinRequestId) {
        JoinRequest jr = joinRequestRepository.findWithRoomById(joinRequestId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.JOIN_REQUEST_NOT_FOUND));

        StudyRoom room = jr.getRoom();
        requireOwner(actorId, room);

        if (!jr.isPending()) {
            throw new BaseException(BaseResponseStatus.JOIN_REQUEST_NOT_PENDING);
        }

        // MVP: 모집(DRAFT) 상태에서만 승인
        if (room.getStatus() != RoomStatus.DRAFT) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_DRAFT);
        }

        if (membershipRepository.existsByRoom_IdAndUserId(room.getId(), jr.getUserId())) {
            throw new BaseException(BaseResponseStatus.MEMBERSHIP_ALREADY_EXISTS);
        }

        List<ParticipationPlatform> participationPlatforms = jr.getPlatforms().stream()
                .map(p -> toParticipationPlatform(jr.getUserId(), p))
                .toList();

        RoomMembership membership = RoomMembership.member(room, jr.getUserId(), participationPlatforms);
        membershipRepository.save(membership);

        jr.approve();

        return new JoinDtos.ApproveResponse(membership.getId());
    }

    @Override
    public void reject(Long actorId, Long joinRequestId) {
        JoinRequest jr = joinRequestRepository.findWithRoomById(joinRequestId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.JOIN_REQUEST_NOT_FOUND));

        requireOwner(actorId, jr.getRoom());

        if (!jr.isPending()) {
            throw new BaseException(BaseResponseStatus.JOIN_REQUEST_NOT_PENDING);
        }

        jr.reject();
    }

    @Override
    public void cancel(Long userId, Long joinRequestId) {
        JoinRequest jr = joinRequestRepository.findWithRoomById(joinRequestId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.JOIN_REQUEST_NOT_FOUND));

        if (!Objects.equals(jr.getUserId(), userId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }
        if (!jr.isPending()) {
            throw new BaseException(BaseResponseStatus.JOIN_REQUEST_NOT_PENDING);
        }

        jr.cancel();
    }

    private void validateInviteOrListing(StudyRoom room, String inviteCode) {
        if (room.getVisibility() == RoomVisibility.PRIVATE) {
            if (inviteCode == null || inviteCode.isBlank()) {
                throw new BaseException(BaseResponseStatus.ROOM_INVITE_CODE_REQUIRED);
            }
            if (!Objects.equals(room.getInviteCode(), inviteCode)) {
                throw new BaseException(BaseResponseStatus.ROOM_INVITE_CODE_MISMATCH);
            }
            return;
        }

        // PUBLIC
        if (inviteCode != null && !inviteCode.isBlank()) {
            if (!Objects.equals(room.getInviteCode(), inviteCode)) {
                throw new BaseException(BaseResponseStatus.ROOM_INVITE_CODE_MISMATCH);
            }
            return;
        }

        // inviteCode가 없으면 "게시판 노출(listed)"이어야 신청 가능
        if (!room.isListed()) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_PUBLIC_LISTED);
        }
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
            boolean exists = userPlatformAccountRepository.existsByUserIdAndPlatform(userId, p);
            if (!exists) missing.add(p);
        }
        if (!missing.isEmpty()) {
            String msg = "선택한 플랫폼 계정을 먼저 등록해주세요. missing=" + missing;
            throw new BaseException(BaseResponseStatus.PLATFORM_ACCOUNT_REQUIRED, msg);
        }
    }

    private ParticipationPlatform toParticipationPlatform(Long userId, Platform platform) {
        UserPlatformAccount account = userPlatformAccountRepository.findByUserIdAndPlatform(userId, platform)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PLATFORM_ACCOUNT_REQUIRED));

        return new ParticipationPlatform(platform, account.getId(), account.getHandle());
    }

    private void requireOwner(Long actorId, StudyRoom room) {
        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }
    }

    private Set<Platform> normalizePlatforms(List<Platform> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "platforms는 최소 1개 이상 필요합니다.");
        }
        return new LinkedHashSet<>(platforms);
    }
}
