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
}
