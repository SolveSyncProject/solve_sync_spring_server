package com.project.solvesync.domain.membership.service;

import com.project.solvesync.domain.membership.dto.MembershipDtos;

import java.util.List;

public interface MembershipService {

    List<MembershipDtos.MemberItem> listMembers(Long actorId, Long roomId);

    void leaveRoom(Long actorId, Long roomId);

    MembershipDtos.KickResponse kickMember(Long actorId, Long roomId, Long targetUserId);
}
