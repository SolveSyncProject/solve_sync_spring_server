package com.project.solvesync.domain.join.dto;

import com.project.solvesync.domain.common.Platform;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class JoinDtos {

    /** 신청 (public 게시판 신청/코드 신청 모두 이걸로) */
    public record CreateRequest(
            String introText,
            String inviteCode,        // PRIVATE 방 코드 신청 시 필수, PUBLIC 게시판 신청은 null 허용
            @NotEmpty List<Platform> platforms
    ) {}

    public record Response(
            Long id,
            Long roomId,
            Long userId,
            String introText,
            String status,
            List<Platform> platforms
    ) {}

    public record ApproveResponse(Long membershipId) {}
}
