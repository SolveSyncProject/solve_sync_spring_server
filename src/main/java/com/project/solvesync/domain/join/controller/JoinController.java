package com.project.solvesync.domain.join.controller;

import com.project.solvesync.domain.join.dto.JoinDtos;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;
import com.project.solvesync.domain.join.service.JoinService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JoinController {

    private final JoinService joinService;

    /**
     * 방 참여 신청
     * - PRIVATE 방: inviteCode 필수
     * - PUBLIC 방: listed=true 이면 inviteCode 없이도 신청 가능
     */
    @PostMapping("/rooms/{roomId}/join-requests")
    public BaseResponse<Void> requestJoin(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long roomId,
            @Valid @RequestBody JoinDtos.CreateRequest request
    ) {
        joinService.requestJoin(userId, roomId, request);
        return BaseResponse.success();
    }

    /**
     * 방장: 참여 신청 목록 조회
     * status가 없으면 전체 조회
     */
    @GetMapping("/rooms/{roomId}/join-requests")
    public BaseResponse<List<JoinDtos.JoinRequestItem>> listJoinRequests(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long roomId,
            @RequestParam(required = false) JoinRequestStatus status
    ) {
        return BaseResponse.success(joinService.listJoinRequests(userId, roomId, status));
    }

    /** 방장: 신청 승인 → 멤버십 생성 */
    @PostMapping("/join-requests/{joinRequestId}/approve")
    public BaseResponse<JoinDtos.ApproveResponse> approve(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long joinRequestId
    ) {
        return BaseResponse.success(joinService.approve(userId, joinRequestId));
    }

    /** 방장: 신청 거절 */
    @PostMapping("/join-requests/{joinRequestId}/reject")
    public BaseResponse<Void> reject(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long joinRequestId
    ) {
        joinService.reject(userId, joinRequestId);
        return BaseResponse.success();
    }

    /** 신청자: 신청 취소 */
    @PostMapping("/join-requests/{joinRequestId}/cancel")
    public BaseResponse<Void> cancel(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long joinRequestId
    ) {
        joinService.cancel(userId, joinRequestId);
        return BaseResponse.success();
    }
}
