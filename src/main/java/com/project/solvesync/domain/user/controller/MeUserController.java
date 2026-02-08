package com.project.solvesync.domain.user.controller;

import com.project.solvesync.domain.user.dto.UserDtos;
import com.project.solvesync.domain.user.service.UserService;
import com.project.solvesync.global.exception.BaseResponse;
import com.project.solvesync.global.security.auth.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MeUserController {

    private final UserService userService;

    @GetMapping
    public BaseResponse<UserDtos.MeResponse> me(@AuthenticationPrincipal AuthUser me) {
        return BaseResponse.success(userService.getMe(me.userId()));
    }

    /** username(공개 식별자) 수정 */
    @PutMapping("/username")
    public BaseResponse<UserDtos.UpdateUsernameResponse> updateUsername(
            @AuthenticationPrincipal AuthUser me,
            @Valid @RequestBody UserDtos.UpdateUsernameRequest request
    ) {
        return BaseResponse.success(userService.updateUsername(me.userId(), request));
    }
}
