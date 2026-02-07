package com.project.solvesync.domain.user.controller;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.user.dto.PlatformAccountRegistryDtos;
import com.project.solvesync.domain.user.entity.User;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.domain.user.repository.UserRepository;
import com.project.solvesync.global.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FastAPI(수집 서버)가 SolveSync로부터 "현재 수집 대상 목록"을 pull 할 수 있도록 제공하는 내부 API.
 *
 * - 주 운영 흐름은 push 이지만, FastAPI가 재기동되거나 레지스트리가 유실될 때
 *   전체를 한 번 가져와 복구할 수 있는 안전장치.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/registry/platform-accounts")
public class InternalPlatformAccountRegistryController {

    private final UserPlatformAccountRepository userPlatformAccountRepository;
    private final UserRepository userRepository;

    @GetMapping
    public BaseResponse<List<PlatformAccountRegistryDtos.RegistryItem>> list(
            @RequestParam(value = "platform", required = false) Platform platform
    ) {
        List<UserPlatformAccount> accounts = (platform == null)
                ? userPlatformAccountRepository.findAll()
                : userPlatformAccountRepository.findAll().stream()
                .filter(a -> a.getPlatform() == platform)
                .sorted(Comparator.comparing(UserPlatformAccount::getId))
                .toList();

        Set<Long> userIds = accounts.stream().map(UserPlatformAccount::getUserId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<PlatformAccountRegistryDtos.RegistryItem> result = accounts.stream()
                .map(acc -> new PlatformAccountRegistryDtos.RegistryItem(
                        acc.getId(),
                        acc.getUserId(),
                        Optional.ofNullable(users.get(acc.getUserId())).map(User::getUsername).orElse(null),
                        acc.getPlatform(),
                        acc.getHandle(),
                        acc.getUpdatedAt()
                ))
                .toList();

        return BaseResponse.success(result);
    }
}
