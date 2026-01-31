package com.project.solvesync.domain.room.service;

import com.project.solvesync.domain.room.dto.RoomDtos;

import java.util.List;

public interface RoomService {

    RoomDtos.CreateResponse createRoom(Long ownerId, RoomDtos.CreateRequest req);

    /**
     * public 게시판용 목록:
     * visibility=PUBLIC, listed=true, status=DRAFT (MVP 기준)
     */
    List<RoomDtos.PublicRoomItem> listPublicRooms(String keyword, int page, int size);

    RoomDtos.RoomDetail getRoomDetail(Long roomId);

    /**
     * 방장만 활성화 가능
     * DRAFT -> ACTIVE 전이 + activatedAt 세팅
     */
    RoomDtos.ActivateResponse activateRoom(Long actorId, Long roomId);
}
