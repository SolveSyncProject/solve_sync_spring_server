package com.project.solvesync.domain.membership.repository;

import com.project.solvesync.domain.membership.entity.MembershipStatus;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.common.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {

    Optional<RoomMembership> findByRoom_IdAndUserId(Long roomId, Long userId);

    Optional<RoomMembership> findByRoom_IdAndUserIdAndStatus(Long roomId, Long userId, MembershipStatus status);

    boolean existsByRoom_IdAndUserId(Long roomId, Long userId);

    boolean existsByRoom_IdAndUserIdAndStatus(Long roomId, Long userId, MembershipStatus status);

    @Query("select distinct m from RoomMembership m left join fetch m.platforms where m.room.id = :roomId")
    List<RoomMembership> findAllWithPlatformsByRoomId(Long roomId);

    /**
     * (room, platform, handleSnapshot) 기반으로 현재 ACTIVE 멤버십을 찾기.
     * 풀이 이벤트가 handle로 들어오는 경우 매핑에 사용.
     */
    @Query("select m from RoomMembership m join m.platforms p " +
            "where m.room.id = :roomId and m.status = :status and p.platform = :platform and p.handleSnapshot = :handleSnapshot")
    Optional<RoomMembership> findByRoomIdAndStatusAndPlatformAndHandleSnapshot(
            Long roomId,
            MembershipStatus status,
            Platform platform,
            String handleSnapshot
    );

    List<RoomMembership> findAllByRoom_Id(Long roomId);
}
