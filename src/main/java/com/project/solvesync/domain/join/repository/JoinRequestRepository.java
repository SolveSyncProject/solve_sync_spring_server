package com.project.solvesync.domain.join.repository;

import com.project.solvesync.domain.join.entity.JoinRequest;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    boolean existsByRoom_IdAndUserIdAndStatus(Long roomId, Long userId, JoinRequestStatus status);

    List<JoinRequest> findAllByRoom_IdOrderByIdDesc(Long roomId);

    List<JoinRequest> findAllByRoom_IdAndStatusOrderByIdDesc(Long roomId, JoinRequestStatus status);

    @EntityGraph(attributePaths = {"room"})
    Optional<JoinRequest> findWithRoomById(Long id);

    /** 신청자: 내가 만든 신청 목록 (room까지 함께 로딩) */
    @EntityGraph(attributePaths = {"room"})
    List<JoinRequest> findAllByUserIdOrderByIdDesc(Long userId);

    /** 신청자: 내가 만든 신청 목록 (status 필터) */
    @EntityGraph(attributePaths = {"room"})
    List<JoinRequest> findAllByUserIdAndStatusOrderByIdDesc(Long userId, JoinRequestStatus status);
}
