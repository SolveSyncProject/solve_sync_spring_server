package com.project.solvesync.domain.solve.controller;

import com.project.solvesync.domain.solve.dto.DailySummaryDtos;
import com.project.solvesync.domain.solve.service.DailySummaryService;
import com.project.solvesync.global.exception.BaseResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Swagger에서 DailySummary 저장 결과를 확인하기 위한 개발용 API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/daily-summaries")
public class DevDailySummaryController {

    private final DailySummaryService dailySummaryService;

    @GetMapping
    public BaseResponse<List<DailySummaryDtos.SummaryView>> list(
            @RequestParam("userPlatformAccountId") @NotNull Long userPlatformAccountId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return BaseResponse.success(dailySummaryService.getSummaries(userPlatformAccountId, from, to));
    }
}
