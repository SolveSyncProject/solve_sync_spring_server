package com.project.solvesync.domain.room.service;

import com.project.solvesync.domain.room.dto.RoomDtos;

import java.util.List;

public interface RoomService {

    RoomDtos.CreateResponse createRoom(Long ownerId, RoomDtos.CreateRequest req);

    /**
     * public 게시판용 룸 목록
     * visibility=PUBLIC, listed=true, status=DRAFT (MVP 기준)
     */
    List<RoomDtos.PublicRoomItem> listPublicRooms(String keyword, int page, int size);

    RoomDtos.RoomDetail getRoomDetail(Long roomId);

    RoomDtos.ActivateResponse activateRoom(Long actorId, Long roomId);
}
