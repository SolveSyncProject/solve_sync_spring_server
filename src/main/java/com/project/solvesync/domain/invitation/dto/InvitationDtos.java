package com.project.solvesync.domain.invitation.dto;

import com.project.solvesync.domain.common.Platform;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public class InvitationDtos {

    public record CreateRequest(
            @NotNull Long inviteeUserId,
            String message
    ) {}

    public record Response(
            Long id,
            Long roomId,
            Long inviterId,
            Long inviteeUserId,
            String status,
            String message,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt
    ) {}

    public record AcceptRequest(
            @NotEmpty List<Platform> platforms // 수락하면서 이 방에서 참여할 플랫폼 선택
    ) {}

    public record AcceptResponse(Long membershipId) {}
}
