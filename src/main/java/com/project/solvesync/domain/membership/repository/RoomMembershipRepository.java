package com.project.solvesync.domain.membership.repository;

import com.project.solvesync.domain.membership.entity.MembershipStatus;
import com.project.solvesync.domain.membership.entity.RoomMembership;
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

    List<RoomMembership> findAllByRoom_Id(Long roomId);
}
