package com.project.solvesync.domain.membership.controller;

import com.project.solvesync.domain.membership.dto.MembershipDtos;
import com.project.solvesync.domain.membership.service.MembershipService;
import com.project.solvesync.global.exception.BaseResponse;
import com.project.solvesync.global.security.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/members")
    public BaseResponse<List<MembershipDtos.MemberItem>> listMembers(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long roomId
    ) {
        return BaseResponse.success(membershipService.listMembers(me.userId(), roomId));
    }

    @PostMapping("/members/leave")
    public BaseResponse<Void> leave(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long roomId
    ) {
        membershipService.leaveRoom(me.userId(), roomId);
        return BaseResponse.success();
    }

    @PostMapping("/members/{targetUserId}/kick")
    public BaseResponse<MembershipDtos.KickResponse> kick(
            @AuthenticationPrincipal AuthUser me,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId
    ) {
        return BaseResponse.success(membershipService.kickMember(me.userId(), roomId, targetUserId));
    }
}
