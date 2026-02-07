package com.project.solvesync.domain.user.event;

import com.project.solvesync.domain.user.entity.User;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.domain.user.repository.UserRepository;
import com.project.solvesync.global.integration.collector.CollectorClient;
import com.project.solvesync.global.integration.collector.PlatformAccountUpsertPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPlatformAccountUpsertedListener {

    private final UserPlatformAccountRepository userPlatformAccountRepository;
    private final UserRepository userRepository;
    private final CollectorClient collectorClient;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(UserPlatformAccountUpsertedEvent event) {
        Optional<UserPlatformAccount> opt = userPlatformAccountRepository.findById(event.userPlatformAccountId());
        if (opt.isEmpty()) {
            log.warn("UserPlatformAccount not found. id={}", event.userPlatformAccountId());
            return;
        }

        UserPlatformAccount acc = opt.get();
        String username = userRepository.findById(acc.getUserId())
                .map(User::getUsername)
                .orElse(null);

        PlatformAccountUpsertPayload payload = new PlatformAccountUpsertPayload(
                acc.getId(),
                acc.getUserId(),
                username,
                acc.getPlatform(),
                acc.getHandle(),
                acc.getUpdatedAt(),
                event.created()
        );

        collectorClient.upsertPlatformAccount(payload);
    }
}
