package com.project.solvesync.domain.invitation.dto;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.invitation.entity.InvitationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class InvitationDtos {

    /** 방장이 특정 username을 초대 */
    public record CreateRequest(
            @NotBlank(message = "inviteeUsername은 필수입니다.")
            String inviteeUsername,

            /**
             * 초대 메시지(서로를 설명할 텍스트) - 선택
             */
            String message
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
            String message,
            InvitationStatus status
    ) {}

    public record AcceptResponse(Long membershipId) {}
}
