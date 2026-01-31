package com.project.solvesync.domain.room.controller;

import com.project.solvesync.domain.room.dto.RoomDtos;
import com.project.solvesync.domain.room.service.RoomService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public BaseResponse<RoomDtos.CreateResponse> create(
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestBody @Valid RoomDtos.CreateRequest req
    ) {
        return BaseResponse.success(roomService.createRoom(ownerId, req));
    }

    /** public 게시판 */
    @GetMapping("/public")
    public BaseResponse<List<RoomDtos.PublicRoomItem>> listPublic(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return BaseResponse.success(roomService.listPublicRooms(keyword, page, size));
    }

    @GetMapping("/{roomId}")
    public BaseResponse<RoomDtos.RoomDetail> detail(@PathVariable Long roomId) {
        return BaseResponse.success(roomService.getRoomDetail(roomId));
    }

    /** 모집 완료 후 방장 활성화 */
    @PostMapping("/{roomId}/activate")
    public BaseResponse<RoomDtos.ActivateResponse> activate(
            @RequestHeader("X-User-Id") Long actorId,
            @PathVariable Long roomId
    ) {
        return BaseResponse.success(roomService.activateRoom(actorId, roomId));
    }
}
