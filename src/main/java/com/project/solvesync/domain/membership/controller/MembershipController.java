package com.project.solvesync.domain.membership.controller;

import com.project.solvesync.domain.membership.dto.MembershipDtos;
import com.project.solvesync.domain.membership.service.MembershipService;
import com.project.solvesync.global.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/members")
    public BaseResponse<List<MembershipDtos.MemberItem>> listMembers(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long roomId
    ) {
        return BaseResponse.success(membershipService.listMembers(actorId, roomId));
    }

    @PostMapping("/members/leave")
    public BaseResponse<Void> leave(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long roomId
    ) {
        membershipService.leaveRoom(actorId, roomId);
        return BaseResponse.success();
    }

    @PostMapping("/members/{targetUserId}/kick")
    public BaseResponse<MembershipDtos.KickResponse> kick(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId
    ) {
        return BaseResponse.success(membershipService.kickMember(actorId, roomId, targetUserId));
    }
}
