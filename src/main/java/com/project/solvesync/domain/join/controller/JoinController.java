package com.project.solvesync.domain.join.controller;

import com.project.solvesync.domain.join.dto.JoinDtos;
import com.project.solvesync.domain.join.service.JoinService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/api/rooms/{roomId}/join-requests")
    public BaseResponse<Void> requestJoin(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long roomId,
            @RequestBody @Valid JoinDtos.CreateRequest req
    ) {
        joinService.requestJoin(userId, roomId, req);
        return BaseResponse.success();
    }

    /** 방장: 신청 목록 */
    @GetMapping("/api/rooms/{roomId}/join-requests")
    public BaseResponse<List<JoinDtos.Response>> listRequests(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "PENDING") String status
    ) {
        return BaseResponse.success(joinService.listRequests(actorId, roomId, status));
    }

    @PostMapping("/api/join-requests/{joinRequestId}/approve")
    public BaseResponse<JoinDtos.ApproveResponse> approve(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long joinRequestId
    ) {
        return BaseResponse.success(joinService.approve(actorId, joinRequestId));
    }

    @PostMapping("/api/join-requests/{joinRequestId}/reject")
    public BaseResponse<Void> reject(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long joinRequestId
    ) {
        joinService.reject(actorId, joinRequestId);
        return BaseResponse.success();
    }
}
