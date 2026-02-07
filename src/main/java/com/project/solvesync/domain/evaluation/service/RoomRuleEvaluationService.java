package com.project.solvesync.domain.evaluation.service;

import com.project.solvesync.domain.evaluation.dto.EvaluationDtos;

public interface RoomRuleEvaluationService {

    EvaluationDtos.RoomEvaluationMatrix getRoomEvaluationMatrix(Long roomId);

    /** 내부/개발 편의용 */
    EvaluationDtos.RunNowResponse runEvaluationNow(Long roomId);
}
