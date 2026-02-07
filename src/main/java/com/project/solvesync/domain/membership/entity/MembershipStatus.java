package com.project.solvesync.domain.membership.entity;

public enum MembershipStatus {
    ACTIVE,
    /** 본인이 자발적으로 탈퇴 */
    LEFT,
    /** 방장에 의해 강퇴 */
    KICKED,
    /** 제재/차단 (추후 정책 확장용) */
    BANNED
}
