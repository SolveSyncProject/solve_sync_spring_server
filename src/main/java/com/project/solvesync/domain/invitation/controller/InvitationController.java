package com.project.solvesync.domain.invitation.controller;

import com.project.solvesync.domain.invitation.dto.InvitationDtos;
import com.project.solvesync.domain.invitation.service.InvitationService;
import com.project.solvesync.global.exception.BaseResponse;
import com.project.solvesync.global.security.auth.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InvitationController {

    private final InvitationService invitationService;

    /** 방장: 특정 userId를 초대 */
    @PostMapping("/rooms/{roomId}/invitations")
    public BaseResponse<Void> invite(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long roomId,
            @Valid @RequestBody InvitationDtos.CreateRequest request
    ) {
        invitationService.invite(me.userId(), roomId, request);
        return BaseResponse.success();
    }

    /** 방장: 방의 초대 목록 조회 */
    @GetMapping("/rooms/{roomId}/invitations")
    public BaseResponse<List<InvitationDtos.InvitationItem>> listRoomInvitations(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long roomId
    ) {
        return BaseResponse.success(invitationService.listRoomInvitations(me.userId(), roomId));
    }

    /** 초대받은 사람: 내 초대 목록 */
    @GetMapping("/me/invitations")
    public BaseResponse<List<InvitationDtos.InvitationItem>> listMyInvitations(
            @AuthenticationPrincipal AuthUser me
    ) {
        return BaseResponse.success(invitationService.listMyInvitations(me.userId()));
    }

    /** 초대 수락(플랫폼 선택) → 멤버십 생성 */
    @PostMapping("/invitations/{invitationId}/accept")
    public BaseResponse<InvitationDtos.AcceptResponse> accept(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long invitationId,
            @Valid @RequestBody InvitationDtos.AcceptRequest request
    ) {
        return BaseResponse.success(invitationService.accept(me.userId(), invitationId, request));
    }

    /** 초대 거절 */
    @PostMapping("/invitations/{invitationId}/decline")
    public BaseResponse<Void> decline(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long invitationId
    ) {
        invitationService.decline(me.userId(), invitationId);
        return BaseResponse.success();
    }

    /** 방장: 초대 취소 */
    @PostMapping("/invitations/{invitationId}/cancel")
    public BaseResponse<Void> cancel(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long invitationId
    ) {
        invitationService.cancel(me.userId(), invitationId);
        return BaseResponse.success();
    }
}
