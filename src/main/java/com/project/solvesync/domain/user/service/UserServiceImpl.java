package com.project.solvesync.domain.user.service;

import com.project.solvesync.domain.user.dto.UserDtos;
import com.project.solvesync.domain.user.entity.User;
import com.project.solvesync.domain.user.repository.UserRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDtos.DevCreateResponse devCreate(UserDtos.DevCreateRequest request) {
        if (request.username() != null && !request.username().isBlank()) {
            String normalized = request.username().trim();
            if (userRepository.existsByUsername(normalized)) {
                throw new BaseException(BaseResponseStatus.USERNAME_ALREADY_TAKEN);
            }
        }

        User user = User.create(request.email(), request.username());
        userRepository.save(user);

        return new UserDtos.DevCreateResponse(user.getId(), user.getEmail(), user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDtos.MeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));
        return new UserDtos.MeResponse(user.getId(), user.getEmail(), user.getUsername());
    }

    @Override
    public UserDtos.UpdateUsernameResponse updateUsername(Long userId, UserDtos.UpdateUsernameRequest request) {
        String username = request.username().trim();
        if (username.isBlank()) {
            throw new BaseException(BaseResponseStatus.USERNAME_REQUIRED);
        }

        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new BaseException(BaseResponseStatus.USERNAME_ALREADY_TAKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        user.updateUsername(username);
        return new UserDtos.UpdateUsernameResponse(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsernameOrThrow(String username) {
        if (username == null || username.isBlank()) {
            throw new BaseException(BaseResponseStatus.USERNAME_REQUIRED);
        }
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USERNAME_NOT_FOUND));
    }
}
