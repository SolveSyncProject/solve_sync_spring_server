package com.project.solvesync.domain.invitation.service;

import com.project.solvesync.domain.invitation.dto.InvitationDtos;

import java.util.List;

public interface InvitationService {

    /**
     * 방장(또는 manager)이 유저ID로 초대:
     * - 초대 대상 유저 존재해야 함
     * - 이미 멤버면 초대 불가
     * - 기존 PENDING 초대가 있으면 중복 방지 정책(에러 or overwrite)
     */
    void invite(Long actorId, Long roomId, InvitationDtos.CreateRequest req);

    /**
     * 나에게 온 초대 목록
     */
    List<InvitationDtos.Response> listMyInvitations(Long userId, String status);

    /**
     * 초대 수락:
     * - invitee 본인만 가능
     * - PENDING만 가능
     * - platforms ⊆ 룰플랫폼
     * - user가 선택 플랫폼 계정을 보유해야 함
     * - 수락 시 membership 생성 + invitation ACCEPTED
     */
    InvitationDtos.AcceptResponse accept(Long userId, Long invitationId, InvitationDtos.AcceptRequest req);

    /**
     * 초대 거절:
     * - invitee 본인만 가능
     * - PENDING만 가능
     */
    void decline(Long userId, Long invitationId);
}
