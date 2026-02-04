package com.project.solvesync.domain.join.dto;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class JoinDtos {

    /**
     * 참여 신청
     * - PRIVATE 방: inviteCode 필수
     * - PUBLIC 방:
     *    - inviteCode를 주면 코드 신청(일치해야 함)
     *    - inviteCode가 없으면 listed=true(공개 게시판 노출)인 방만 신청 가능
     * - platforms: "이 방에서 참여할 플랫폼" (복수)
     */
    public record CreateRequest(
            String introText,
            String inviteCode,
            @NotEmpty(message = "platforms는 최소 1개 이상이어야 합니다.")
            List<Platform> platforms
    ) {}

    /**
     * 방장용 신청 목록 아이템
     */
    public record JoinRequestItem(
            Long joinRequestId,
            Long applicantUserId,
            String introText,
            JoinRequestStatus status,
            List<Platform> platforms
    ) {}

    /**
     * 승인 응답: 멤버십 생성 결과
     */
    public record ApproveResponse(Long membershipId) {}
}
