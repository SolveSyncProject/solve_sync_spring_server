package com.project.solvesync.domain.user.service;

import com.project.solvesync.domain.user.dto.UserDtos;
import com.project.solvesync.domain.user.entity.User;

public interface UserService {

    UserDtos.DevCreateResponse devCreate(UserDtos.DevCreateRequest request);

    UserDtos.MeResponse getMe(Long userId);

    UserDtos.UpdateUsernameResponse updateUsername(Long userId, UserDtos.UpdateUsernameRequest request);

    User getByUsernameOrThrow(String username);
}
