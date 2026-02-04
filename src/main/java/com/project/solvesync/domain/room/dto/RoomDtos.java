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
            @NotBlank @Size(max = 80) String name,
            @NotNull RoomVisibility visibility,
            boolean listed,
            @NotBlank @Size(max = 40) String timezone,
            @NotNull OffsetDateTime startAt,

            @Valid @NotNull Rule rule,
            @Valid @NotEmpty List<RulePlatform> rulePlatforms,

            @NotEmpty List<Platform> ownerPlatforms
    ) {}

    public record Rule(
            @NotNull PeriodUnit periodUnit,
            @Min(0) @Max(100) int requiredCount,
            boolean includeHolidays
    ) {}

    public record RulePlatform(
            @NotNull Platform platform,
            @Min(-1) Integer tierMin,   // null or -1 = 미지정
            @Min(-1) Integer tierMax
    ) {}

    public record CreateResponse(Long roomId, String inviteCode, RoomStatus status) {}

    /** ✅ 공개방 리스트에서 사용할 아이템 (RoomService가 참조하므로 반드시 존재해야 함) */
    public record PublicRoomItem(
            Long roomId,
            String name,
            RoomStatus status,
            String timezone
    ) {}

    public record RoomDetail(
            Long roomId,
            Long ownerId,
            String name,
            RoomStatus status,
            RoomVisibility visibility,
            boolean listed,
            String inviteCode,
            String timezone,
            OffsetDateTime startAt,
            OffsetDateTime activatedAt,
            Rule rule,
            List<RulePlatform> rulePlatforms
    ) {}

    public record ActivateResponse(Long roomId, RoomStatus status, OffsetDateTime activatedAt) {}
}
