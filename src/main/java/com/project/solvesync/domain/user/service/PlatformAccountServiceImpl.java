package com.project.solvesync.domain.user.service;

import com.project.solvesync.domain.user.dto.PlatformAccountDtos;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.event.UserPlatformAccountUpsertedEvent;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import com.project.solvesync.global.maintenance.PlatformAccountMaintenanceGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformAccountServiceImpl implements PlatformAccountService {

    private final UserPlatformAccountRepository userPlatformAccountRepository;
    private final PlatformAccountMaintenanceGuard maintenanceGuard;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void upsert(Long userId, PlatformAccountDtos.UpsertRequest request) {
        // ✅ 01:30~03:00 점검시간 차단
        maintenanceGuard.checkAvailable();

        String handle = request.handle() == null ? null : request.handle().trim();
        if (handle == null || handle.isBlank()) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "handle은 필수입니다.");
        }

        userPlatformAccountRepository.findByUserIdAndPlatform(userId, request.platform())
                .ifPresentOrElse(existing -> {
                    boolean changed = !handle.equals(existing.getHandle());
                    existing.updateHandle(handle);

                    // ✅ 변경이 실제로 있을 때만 이벤트 발행(불필요한 push 방지)
                    if (changed) {
                        eventPublisher.publishEvent(new UserPlatformAccountUpsertedEvent(existing.getId(), false));
                    }
                }, () -> {
                    UserPlatformAccount created = UserPlatformAccount.create(userId, request.platform(), handle);
                    userPlatformAccountRepository.save(created);

                    eventPublisher.publishEvent(new UserPlatformAccountUpsertedEvent(created.getId(), true));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformAccountDtos.Response> list(Long userId) {
        return userPlatformAccountRepository.findAllByUserIdOrderByPlatformAsc(userId).stream()
                .map(a -> new PlatformAccountDtos.Response(a.getPlatform(), a.getHandle()))
                .toList();
    }
}
