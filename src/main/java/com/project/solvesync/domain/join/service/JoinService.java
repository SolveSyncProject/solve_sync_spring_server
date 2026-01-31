package com.project.solvesync.domain.join.service;

import com.project.solvesync.domain.join.dto.JoinDtos;

import java.util.List;

public interface JoinService {

    /**
     * 참여 신청:
     * - PUBLIC 게시판 신청: inviteCode null 허용
     * - PRIVATE 코드 신청: inviteCode 필수 + 일치
     * - selectedPlatforms ⊆ 룰플랫폼
     * - 유저가 selectedPlatforms의 UserPlatformAccount를 보유해야 함
     */
    void requestJoin(Long userId, Long roomId, JoinDtos.CreateRequest req);

    /**
     * 방장 전용:
     * status 필터(PENDING 등)
     */
    List<JoinDtos.Response> listRequests(Long actorId, Long roomId, String status);

    /**
     * 방장 전용 승인:
     * - PENDING만 승인 가능
     * - 승인 시 membership + membershipPlatforms 생성
     */
    JoinDtos.ApproveResponse approve(Long actorId, Long joinRequestId);

    /** 방장 전용 거절 */
    void reject(Long actorId, Long joinRequestId);
}
