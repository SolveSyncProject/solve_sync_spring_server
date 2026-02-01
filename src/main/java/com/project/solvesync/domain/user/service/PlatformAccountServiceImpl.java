package com.project.solvesync.domain.user.service;

import com.project.solvesync.domain.user.dto.PlatformAccountDtos;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformAccountServiceImpl implements PlatformAccountService {

    private final UserPlatformAccountRepository userPlatformAccountRepository;

    @Override
    public void upsert(Long userId, PlatformAccountDtos.UpsertRequest req) {
        userPlatformAccountRepository
                .findByUserIdAndPlatform(userId, req.platform())
                .ifPresentOrElse(
                        existing -> existing.updateHandle(req.handle()),
                        () -> userPlatformAccountRepository.save(
                                UserPlatformAccount.create(userId, req.platform(), req.handle())
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformAccountDtos.Response> list(Long userId) {
        return userPlatformAccountRepository.findAllByUserIdOrderByPlatformAsc(userId)
                .stream()
                .map(a -> new PlatformAccountDtos.Response(a.getPlatform(), a.getHandle()))
                .toList();
    }
}
