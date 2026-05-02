package com.project.solvesync.domain.room.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.membership.entity.ParticipationPlatform;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.room.dto.RoomDtos;
import com.project.solvesync.domain.room.entity.*;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import com.project.solvesync.domain.user.repository.UserPlatformAccountRepository;
import com.project.solvesync.domain.user.repository.UserRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final StudyRoomRepository roomRepository;
    private final UserPlatformAccountRepository userPlatformAccountRepository;
    private final UserRepository userRepository;

    @Override
    public RoomDtos.CreateResponse createRoom(Long ownerId, RoomDtos.CreateRequest req) {

        // 0) 유저 존재 검증
        userRepository.findById(ownerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 1) ownerPlatforms 유효성 + 플랫폼 계정 존재 검증 & 스냅샷 채우기
        if (req.ownerPlatforms() == null || req.ownerPlatforms().isEmpty()) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "ownerPlatforms는 최소 1개 이상 필요합니다.");
        }

        Set<Platform> ownerPlatforms = new LinkedHashSet<>(req.ownerPlatforms());
        if (ownerPlatforms.size() != req.ownerPlatforms().size()) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "ownerPlatforms에 중복 플랫폼이 존재합니다.");
        }

        List<ParticipationPlatform> ownerParticipationPlatforms = new ArrayList<>();
        for (Platform p : ownerPlatforms) {
            UserPlatformAccount account = userPlatformAccountRepository.findByUserIdAndPlatform(ownerId, p)
                    .orElseThrow(() -> new BaseException(
                            BaseResponseStatus.PLATFORM_ACCOUNT_REQUIRED,
                            "방장은 방 생성 전 플랫폼 계정을 등록해야 합니다. missing=" + p
                    ));
            ownerParticipationPlatforms.add(new ParticipationPlatform(p, account.getId(), account.getHandle()));
        }

        // 2) 룰 유효성 검증
        validateRule(req);

        // 3) 룰 플랫폼 검증
        validateRulePlatforms(req.rulePlatforms());
        validateOwnerPlatformsWithinRulePlatforms(ownerPlatforms, req.rulePlatforms());

        // 4) 룸 생성
        StudyRoom room = StudyRoom.create(
                ownerId,
                req.name().trim(),
                req.visibility(),
                req.listed(),
                generateInviteCode(),
                req.timezone().trim(),
                req.startAt()
        );

        // 5) 룰 생성 + attach
        RoomRule rule = RoomRule.create(req.rule().periodUnit(), req.rule().requiredCount(), req.rule().includeHolidays());
        room.attachRule(rule);

        // 6) RulePlatforms attach  (null=미지정 그대로 저장)
        for (RoomDtos.RulePlatform rp : req.rulePlatforms()) {
            RoomRulePlatform entity = RoomRulePlatform.create(rp.platform(), rp.tierMin(), rp.tierMax());
            room.addRulePlatform(entity);
        }

        // 7) OWNER 멤버십 생성 시 플랫폼 스냅샷 채움
        RoomMembership ownerMembership = RoomMembership.owner(room, ownerId, ownerParticipationPlatforms);
        room.addMembership(ownerMembership);

        // 8) save
        roomRepository.save(room);

        return new RoomDtos.CreateResponse(room.getId(), room.getInviteCode(), room.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDtos.PublicRoomItem> listPublicRooms(String keyword, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<StudyRoom> result = roomRepository.searchPublicRooms(
                RoomVisibility.PUBLIC,
                RoomStatus.DRAFT,
                keyword,
                pageable
        );

        return result.stream()
                .map(r -> new RoomDtos.PublicRoomItem(r.getId(), r.getName(), r.getStatus(), r.getTimezone()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDtos.RoomDetail getRoomDetail(Long roomId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        RoomDtos.Rule rule = new RoomDtos.Rule(
                room.getRule().getPeriodUnit(),
                room.getRule().getRequiredCount(),
                room.getRule().isIncludeHolidays()
        );

        List<RoomDtos.RulePlatform> rulePlatforms = room.getRulePlatforms().stream()
                .map(rp -> new RoomDtos.RulePlatform(rp.getPlatform(), rp.getTierMin(), rp.getTierMax()))
                .toList();

        return new RoomDtos.RoomDetail(
                room.getId(),
                room.getOwnerId(),
                room.getName(),
                room.getStatus(),
                room.getVisibility(),
                room.isListed(),
                room.getInviteCode(),
                room.getTimezone(),
                room.getStartAt(),
                room.getActivatedAt(),
                room.getEvaluationStartAt(),
                rule,
                rulePlatforms
        );
    }

    @Override
    public RoomDtos.ActivateResponse activateRoom(Long actorId, Long roomId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }
        if (room.getStatus() != RoomStatus.DRAFT) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_DRAFT);
        }

        room.activateNow();
        return new RoomDtos.ActivateResponse(room.getId(), room.getStatus(), room.getActivatedAt(), room.getEvaluationStartAt());
    }

    private void validateRule(RoomDtos.CreateRequest req) {
        int requiredCount = req.rule().requiredCount();
        if (requiredCount < 0 || requiredCount > 100) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "requiredCount는 0~100 범위여야 합니다.");
        }
        if (req.rule().periodUnit() == null) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "periodUnit은 필수입니다.");
        }
    }

    private void validateRulePlatforms(List<RoomDtos.RulePlatform> rulePlatforms) {
        if (rulePlatforms == null || rulePlatforms.isEmpty()) {
            throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "rulePlatforms는 최소 1개 이상 필요합니다.");
        }

        Set<Platform> seen = new HashSet<>();
        for (RoomDtos.RulePlatform rp : rulePlatforms) {
            if (rp.platform() == null) {
                throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "rulePlatforms.platform은 필수입니다.");
            }
            if (!seen.add(rp.platform())) {
                throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "rulePlatforms에 중복 플랫폼이 존재합니다: " + rp.platform());
            }

            Integer min = rp.tierMin();
            Integer max = rp.tierMax();

            if (min != null && min < 0) {
                throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "tierMin은 0 이상 또는 null 이어야 합니다. platform=" + rp.platform());
            }
            if (max != null && max < 0) {
                throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "tierMax는 0 이상 또는 null 이어야 합니다. platform=" + rp.platform());
            }
            if (min != null && max != null && min > max) {
                throw new BaseException(BaseResponseStatus.VALIDATION_ERROR, "tierMin <= tierMax 이어야 합니다. platform=" + rp.platform());
            }
        }
    }

    private void validateOwnerPlatformsWithinRulePlatforms(Set<Platform> ownerPlatforms, List<RoomDtos.RulePlatform> rulePlatforms) {
        Set<Platform> allowed = rulePlatforms.stream()
                .map(RoomDtos.RulePlatform::platform)
                .collect(java.util.stream.Collectors.toSet());

        if (!allowed.containsAll(ownerPlatforms)) {
            throw new BaseException(
                    BaseResponseStatus.INVALID_PLATFORM_SELECTION,
                    "ownerPlatforms는 rulePlatforms에 포함된 플랫폼만 선택할 수 있습니다."
            );
        }
    }

    private String generateInviteCode() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return raw.substring(0, 10);
    }
}
