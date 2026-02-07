package com.project.solvesync.domain.evaluation.entity;

/**
 * 방 규칙 평가 결과(칸) 상태.
 *
 * - ACHIEVED / FAILED: 해당 기간(period) 규칙에 대해 실제로 평가가 수행된 상태
 * - NOT_JOINED: 해당 기간에 아직 방에 가입 전(또는 해당 기간에는 멤버가 아니었던 상태)
 * - LEFT/KICKED/BANNED: 해당 기간 종료 시점 기준으로 멤버가 아니어서 "탈퇴 사유"를 표시
 */
public enum EvaluationCellStatus {
    ACHIEVED,
    FAILED,
    NOT_JOINED,
    LEFT,
    KICKED,
    BANNED
}
