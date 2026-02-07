package com.project.solvesync.domain.solve.controller;

import com.project.solvesync.domain.solve.dto.DailySummaryDtos;
import com.project.solvesync.domain.solve.service.DailySummaryService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * FastAPI(수집 서버) -> SolveSync
 *
 * - 여기서는 DB에 바로 쓰지 않는다.
 * - MQ(RabbitMQ)에 적재하고, 컨슈머가 비동기 upsert를 수행한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/daily-summaries")
public class InternalDailySummaryController {

    private final DailySummaryService dailySummaryService;

    @PostMapping("/publish")
    public BaseResponse<DailySummaryDtos.UpsertBatchResponse> publish(
            @Valid @RequestBody DailySummaryDtos.UpsertBatchRequest request
    ) {
        int accepted = dailySummaryService.publishBatch(request);
        return BaseResponse.success(new DailySummaryDtos.UpsertBatchResponse(accepted));
    }
}
