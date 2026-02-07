package com.project.solvesync.domain.evaluation.controller;

import com.project.solvesync.domain.evaluation.dto.EvaluationDtos;
import com.project.solvesync.domain.evaluation.service.RoomRuleEvaluationService;
import com.project.solvesync.global.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}/evaluations")
public class RoomRuleEvaluationController {

    private final RoomRuleEvaluationService evaluationService;

    @GetMapping("/matrix")
    public BaseResponse<EvaluationDtos.RoomEvaluationMatrix> matrix(@PathVariable Long roomId) {
        return BaseResponse.success(evaluationService.getRoomEvaluationMatrix(roomId));
    }
}
