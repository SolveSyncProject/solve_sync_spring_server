package com.project.solvesync.domain.room.dto;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.room.entity.PeriodUnit;
import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;

public class RoomDtos {

    public record CreateRequest(
            @NotBlank String name,
            String description,
            @NotNull RoomVisibility visibility,
            boolean listed,                 // ✅ public 게시판 노출 여부
            @NotBlank String timezone,
            @NotNull OffsetDateTime startAt,

            @Valid @NotNull Rule rule,
            @Valid @NotEmpty List<RulePlatform> rulePlatforms,

            @NotEmpty List<Platform> ownerPlatforms // 방장이 이 방에서 참여할 플랫폼 선택
    ) {}

    public record Rule(
            @NotNull PeriodUnit periodUnit,
            @Min(1) @Max(10) int requiredCount, // ✅ 플랫폼 합산 카운트
            boolean includeHolidays
    ) {}

    public record RulePlatform(
            @NotNull Platform platform,
            Integer tierMin,
            Integer tierMax
    ) {}

    public record CreateResponse(Long roomId, String inviteCode) {}

    public record PublicRoomItem(
            Long roomId,
            String name,
            String description,
            RoomVisibility visibility,
            boolean listed,
            RoomStatus status,
            String timezone,
            OffsetDateTime startAt
    ) {}

    public record RoomDetail(
            Long roomId,
            Long ownerId,
            String name,
            String description,
            RoomVisibility visibility,
            boolean listed,
            RoomStatus status,
            String timezone,
            OffsetDateTime startAt,
            OffsetDateTime activatedAt
    ) {}

    public record ActivateResponse(
            Long roomId,
            RoomStatus status,
            OffsetDateTime activatedAt
    ) {}
}
