package com.project.solvesync.domain.room.service;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.membership.entity.ParticipationPlatform;
import com.project.solvesync.domain.membership.entity.RoomMembership;
import com.project.solvesync.domain.room.dto.RoomDtos;
import com.project.solvesync.domain.room.entity.*;
import com.project.solvesync.domain.room.repository.StudyRoomRepository;
import com.project.solvesync.global.exception.BaseException;
import com.project.solvesync.global.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final StudyRoomRepository roomRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray(); // 헷갈리는 문자 제거

    @Override
    public RoomDtos.CreateResponse createRoom(Long ownerId, RoomDtos.CreateRequest req) {

        // 1) listed 정책 검증: PRIVATE는 게시판 노출 불가(원하면 완화 가능)
        if (req.visibility() == RoomVisibility.PRIVATE && req.listed()) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST, "PRIVATE 방은 listed=true로 설정할 수 없습니다.");
        }

        // 2) rulePlatforms 중복 검증
        Set<Platform> rulePlatformSet = new HashSet<>();
        for (RoomDtos.RulePlatform rp : req.rulePlatforms()) {
            if (!rulePlatformSet.add(rp.platform())) {
                throw new BaseException(BaseResponseStatus.BAD_REQUEST, "rulePlatforms에 중복 플랫폼이 존재합니다: " + rp.platform());
            }
        }

        // 3) ownerPlatforms ⊆ rulePlatforms 검증
        Set<Platform> ownerPlatformSet = new HashSet<>(req.ownerPlatforms());
        if (!rulePlatformSet.containsAll(ownerPlatformSet)) {
            throw new BaseException(BaseResponseStatus.INVALID_PLATFORM_SELECTION);
        }

        // 4) invite code 발급
        String inviteCode = generateUniqueInviteCode();

        // 5) Room 생성
        StudyRoom room = StudyRoom.create(
                ownerId,
                req.name(),
                req.description(),
                req.visibility(),
                req.listed(),
                req.timezone(),
                req.startAt(),
                inviteCode
        );

        // 6) Rule attach
        RoomRule rule = RoomRule.create(
                req.rule().periodUnit(),
                req.rule().requiredCount(),
                req.rule().includeHolidays()
        );
        room.attachRule(rule);

        // 7) RulePlatforms attach
        for (RoomDtos.RulePlatform rp : req.rulePlatforms()) {
            RoomRulePlatform entity = RoomRulePlatform.create(rp.platform(), rp.tierMin(), rp.tierMax());
            room.addRulePlatform(entity);
        }

        // 8) Owner membership 생성 + 참여 플랫폼 저장
        List<ParticipationPlatform> ownerPlatforms = ownerPlatformSet.stream()
                .map(ParticipationPlatform::of)
                .collect(Collectors.toList());

        RoomMembership ownerMembership = RoomMembership.owner(room, ownerId, ownerPlatforms);
        room.addMembership(ownerMembership);

        // 9) 저장 (cascade로 rule/rulePlatforms/membership 같이 저장)
        StudyRoom saved = roomRepository.save(room);

        return new RoomDtos.CreateResponse(saved.getId(), saved.getInviteCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDtos.PublicRoomItem> listPublicRooms(String keyword, int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST, "page/size 값이 올바르지 않습니다.");
        }

        List<StudyRoom> rooms = roomRepository.findPublicListedDraftRooms(
                RoomVisibility.PUBLIC,
                RoomStatus.DRAFT,
                keyword,
                PageRequest.of(page, size)
        );

        return rooms.stream()
                .map(this::toPublicItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDtos.RoomDetail getRoomDetail(Long roomId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        return toRoomDetail(room);
    }

    @Override
    public RoomDtos.ActivateResponse activateRoom(Long actorId, Long roomId) {
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ROOM_NOT_FOUND));

        // 권한: 방장만
        if (!Objects.equals(room.getOwnerId(), actorId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }

        // 상태 체크
        if (room.getStatus() == RoomStatus.ENDED) {
            throw new BaseException(BaseResponseStatus.ROOM_ENDED);
        }
        if (room.getStatus() == RoomStatus.ACTIVE) {
            throw new BaseException(BaseResponseStatus.ROOM_ALREADY_ACTIVE);
        }
        if (room.getStatus() != RoomStatus.DRAFT) {
            throw new BaseException(BaseResponseStatus.ROOM_NOT_DRAFT);
        }

        // 활성화
        room.activateNow();

        return new RoomDtos.ActivateResponse(room.getId(), room.getStatus(), room.getActivatedAt());
    }

    private RoomDtos.PublicRoomItem toPublicItem(StudyRoom r) {
        return new RoomDtos.PublicRoomItem(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getVisibility(),
                r.isListed(),
                r.getStatus(),
                r.getTimezone(),
                r.getStartAt()
        );
    }

    private RoomDtos.RoomDetail toRoomDetail(StudyRoom r) {
        return new RoomDtos.RoomDetail(
                r.getId(),
                r.getOwnerId(),
                r.getName(),
                r.getDescription(),
                r.getVisibility(),
                r.isListed(),
                r.getStatus(),
                r.getTimezone(),
                r.getStartAt(),
                r.getActivatedAt()
        );
    }

    private String generateUniqueInviteCode() {
        // 충돌 가능성 낮지만, DB 유니크 + exists 체크로 한 번 더 안전하게
        for (int i = 0; i < 20; i++) {
            String code = randomCode(8);
            if (!roomRepository.existsByInviteCode(code)) {
                return code;
            }
        }
        throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR, "초대코드 생성에 실패했습니다.");
    }

    private String randomCode(int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = CODE_CHARS[RANDOM.nextInt(CODE_CHARS.length)];
        }
        return new String(buf);
    }
}
