package com.project.solvesync.domain.invitation.dto;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.invitation.entity.InvitationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class InvitationDtos {

    /** 방장이 특정 userId를 초대 */
    public record CreateRequest(
            @NotNull(message = "inviteeUserId는 필수입니다.")
            Long inviteeUserId
    ) {}

    /** 초대 수락(이때 참여 플랫폼 선택) */
    public record AcceptRequest(
            @NotEmpty(message = "platforms는 최소 1개 이상이어야 합니다.")
            List<Platform> platforms
    ) {}

    public record InvitationItem(
            Long invitationId,
            Long roomId,
            Long inviterUserId,
            Long inviteeUserId,
            InvitationStatus status
    ) {}

    public record AcceptResponse(Long membershipId) {}
}
