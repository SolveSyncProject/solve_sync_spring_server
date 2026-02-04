package com.project.solvesync.domain.user.dto;

import com.project.solvesync.domain.common.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlatformAccountDtos {

    public record UpsertRequest(
            @NotNull Platform platform,
            @NotBlank String handle
    ) {}

    public record Response(
            Platform platform,
            String handle
    ) {}
}
