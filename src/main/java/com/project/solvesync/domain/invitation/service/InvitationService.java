package com.project.solvesync.domain.invitation.service;

import com.project.solvesync.domain.invitation.dto.InvitationDtos;

import java.util.List;

public interface InvitationService {

    void invite(Long actorId, Long roomId, InvitationDtos.CreateRequest request);

    List<InvitationDtos.InvitationItem> listRoomInvitations(Long actorId, Long roomId);

    List<InvitationDtos.InvitationItem> listMyInvitations(Long userId);

    InvitationDtos.AcceptResponse accept(Long userId, Long invitationId, InvitationDtos.AcceptRequest request);

    void decline(Long userId, Long invitationId);

    void cancel(Long actorId, Long invitationId);
}
