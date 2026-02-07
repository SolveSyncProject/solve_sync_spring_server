package com.project.solvesync.domain.evaluation.controller;

import com.project.solvesync.domain.evaluation.dto.EvaluationDtos;
import com.project.solvesync.domain.evaluation.service.RoomRuleEvaluationService;
import com.project.solvesync.global.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/evaluations")
public class InternalEvaluationController {

    private final RoomRuleEvaluationService evaluationService;

    /** 개발/테스트용: 특정 방에 대해 평가를 즉시 catch-up 실행 */
    @PostMapping("/rooms/{roomId}/run-now")
    public BaseResponse<EvaluationDtos.RunNowResponse> runNow(@PathVariable Long roomId) {
        return BaseResponse.success(evaluationService.runEvaluationNow(roomId));
    }
}
