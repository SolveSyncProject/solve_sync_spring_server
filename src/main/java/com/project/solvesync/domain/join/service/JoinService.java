package com.project.solvesync.domain.join.service;

import com.project.solvesync.domain.join.dto.JoinDtos;
import com.project.solvesync.domain.join.entity.JoinRequestStatus;

import java.util.List;

public interface JoinService {

    void requestJoin(Long userId, Long roomId, JoinDtos.CreateRequest req);

    List<JoinDtos.JoinRequestItem> listJoinRequests(Long actorId, Long roomId, JoinRequestStatus status);

    JoinDtos.ApproveResponse approve(Long actorId, Long joinRequestId);

    void reject(Long actorId, Long joinRequestId);

    void cancel(Long userId, Long joinRequestId);
}
