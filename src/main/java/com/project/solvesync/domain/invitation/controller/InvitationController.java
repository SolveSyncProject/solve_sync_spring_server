package com.project.solvesync.domain.invitation.controller;

import com.project.solvesync.domain.invitation.dto.InvitationDtos;
import com.project.solvesync.domain.invitation.service.InvitationService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /** 방장: 유저ID로 초대 */
    @PostMapping("/api/rooms/{roomId}/invitations")
    public BaseResponse<Void> invite(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long roomId,
            @RequestBody @Valid InvitationDtos.CreateRequest req
    ) {
        invitationService.invite(actorId, roomId, req);
        return BaseResponse.success();
    }

    /** 내 초대 목록 */
    @GetMapping("/api/me/invitations")
    public BaseResponse<List<InvitationDtos.Response>> myInvitations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "PENDING") String status
    ) {
        return BaseResponse.success(invitationService.listMyInvitations(userId, status));
    }

    /** 초대 수락 */
    @PostMapping("/api/invitations/{invitationId}/accept")
    public BaseResponse<InvitationDtos.AcceptResponse> accept(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long invitationId,
            @RequestBody @Valid InvitationDtos.AcceptRequest req
    ) {
        return BaseResponse.success(invitationService.accept(userId, invitationId, req));
    }

    /** 초대 거절 */
    @PostMapping("/api/invitations/{invitationId}/decline")
    public BaseResponse<Void> decline(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long invitationId
    ) {
        invitationService.decline(userId, invitationId);
        return BaseResponse.success();
    }
}
