package com.project.solvesync.domain.join.service;

import com.project.solvesync.domain.join.dto.JoinDtos;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;

import java.util.List;

public interface JoinService {

    void requestJoin(Long userId, Long roomId, JoinDtos.CreateRequest req);

    List<JoinDtos.JoinRequestItem> listJoinRequests(Long actorId, Long roomId, JoinRequestStatus status);

    /** 신청자: 내가 보낸 참가 신청 목록 조회 */
    List<JoinDtos.MyJoinRequestItem> listMyJoinRequests(Long userId, JoinRequestStatus status);

    JoinDtos.ApproveResponse approve(Long actorId, Long joinRequestId);

    void reject(Long actorId, Long joinRequestId);

    void cancel(Long userId, Long joinRequestId);
}
