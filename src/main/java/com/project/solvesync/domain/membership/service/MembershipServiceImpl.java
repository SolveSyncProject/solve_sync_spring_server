package com.project.solvesync.domain.membership.service;

import com.project.solvesync.domain.membership.dto.MembershipDtos;
import com.project.solvesync.domain.membership.entity.*;
import com.project.solvesync.domain.membership.repository.RoomMembershipRepository;
import com.project.solvesync.domain.room.entity.StudyRoom;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImpl implements MembershipService {

    private final StudyRoomRepository roomRepository;
    private final RoomMembershipRepository membershipRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MembershipDtos.MemberItem> listMembers(Long actorId, Long roomId) {

        // 방 존재 확인
        roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        // 권한: 최소한 방 멤버여야 목록 조회 가능(정책 변경 가능)
        RoomMembership actor = membershipRepository.findByRoom_IdAndUserIdAndStatus(roomId, actorId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FORBIDDEN, "방 멤버만 조회할 수 있습니다."));

        // 멤버 목록
        List<RoomMembership> members = membershipRepository.findAllWithPlatformsByRoomId(roomId);

        return members.stream()
                .map(this::toMemberItem)
                .toList();
    }

    @Override
    public void leaveRoom(Long actorId, Long roomId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        RoomMembership membership = membershipRepository.findByRoom_IdAndUserIdAndStatus(roomId, actorId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBERSHIP_NOT_FOUND));

        // 방장은 탈퇴 못하게 막는 게 일반적(정책)
        if (membership.getRole() == RoomRole.OWNER) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST, "방장은 탈퇴할 수 없습니다. (방 종료/양도 정책 필요)");
        }

        // ACTIVE -> LEFT
        membership.leave();
    }

    @Override
    public MembershipDtos.KickResponse kickMember(Long actorId, Long roomId, Long targetUserId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        // 방장 권한 체크: room.ownerId == actorId (간단/명확)
        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }

        RoomMembership target = membershipRepository.findByRoom_IdAndUserIdAndStatus(roomId, targetUserId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBERSHIP_NOT_FOUND, "대상 사용자는 ACTIVE 멤버가 아닙니다."));

        // 방장은 강퇴 불가
        if (target.getRole() == RoomRole.OWNER) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST, "방장은 강퇴할 수 없습니다.");
        }

        target.kick();
        return new MembershipDtos.KickResponse(roomId, targetUserId, target.getStatus());
    }

    private MembershipDtos.MemberItem toMemberItem(RoomMembership m) {
        List<MembershipDtos.PlatformItem> platforms = m.getPlatforms().stream()
                .map(p -> new MembershipDtos.PlatformItem(
                        p.getPlatform(),
                        p.getUserPlatformAccountId(),
                        p.getHandleSnapshot()
                ))
                .toList();

        return new MembershipDtos.MemberItem(
                m.getUserId(),
                m.getRole(),
                m.getStatus(),
                m.getJoinedAt(),
                m.getLeftAt(),
                platforms
        );
    }
}
