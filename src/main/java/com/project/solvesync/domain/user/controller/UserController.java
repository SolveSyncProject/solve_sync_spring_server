package com.project.solvesync.domain.user.controller;

import com.project.solvesync.domain.user.dto.UserDtos;
import com.project.solvesync.domain.user.service.UserService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public BaseResponse<UserDtos.DevCreateResponse> devCreate(@Valid @RequestBody UserDtos.DevCreateRequest request) {
        return BaseResponse.success(userService.devCreate(request));
    }
}
