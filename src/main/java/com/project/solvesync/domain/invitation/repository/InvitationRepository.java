package com.project.solvesync.domain.invitation.repository;

import com.project.solvesync.domain.invitation.entity.Invitation;
import com.project.solvesync.domain.invitation.entity.InvitationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    boolean existsByRoom_IdAndInviteeUserIdAndStatus(Long roomId, Long inviteeUserId, InvitationStatus status);

    List<Invitation> findAllByRoom_IdOrderByIdDesc(Long roomId);

    List<Invitation> findAllByInviteeUserIdOrderByIdDesc(Long inviteeUserId);

    @EntityGraph(attributePaths = {"room"})
    Optional<Invitation> findWithRoomById(Long id);
}
