package com.project.solvesync.domain.user.service;

import com.project.solvesync.domain.user.dto.PlatformAccountDtos;

import java.util.List;

public interface PlatformAccountService {
    void upsert(Long userId, PlatformAccountDtos.UpsertRequest req);
    List<PlatformAccountDtos.Response> list(Long userId);
}
