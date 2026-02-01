package com.project.solvesync.domain.membership.dto;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.membership.entity.MembershipStatus;
import com.project.solvesync.domain.membership.entity.RoomRole;

import java.time.OffsetDateTime;
import java.util.List;

public class MembershipDtos {

    public record PlatformItem(
            Platform platform,
            Long userPlatformAccountId,
            String handleSnapshot
    ) {}

    public record MemberItem(
            Long userId,
            RoomRole role,
            MembershipStatus status,
            OffsetDateTime joinedAt,
            OffsetDateTime leftAt,
            List<PlatformItem> platforms
    ) {}

    public record KickResponse(
            Long roomId,
            Long targetUserId,
            MembershipStatus status
    ) {}
}
