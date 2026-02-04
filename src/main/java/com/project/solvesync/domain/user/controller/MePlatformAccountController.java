package com.project.solvesync.domain.user.controller;

import com.project.solvesync.domain.user.dto.PlatformAccountDtos;
import com.project.solvesync.domain.user.service.PlatformAccountService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/platform-accounts")
public class MePlatformAccountController {

    private final PlatformAccountService platformAccountService;

    /**
     * 특정 플랫폼의 handle 등록/갱신(upsert)
     */
    @PostMapping
    public BaseResponse<Void> upsert(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid PlatformAccountDtos.UpsertRequest req
    ) {
        platformAccountService.upsert(userId, req);
        return BaseResponse.success();
    }

    /**
     * 내 플랫폼 계정 목록 조회
     */
    @GetMapping
    public BaseResponse<List<PlatformAccountDtos.Response>> list(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return BaseResponse.success(platformAccountService.list(userId));
    }
}
