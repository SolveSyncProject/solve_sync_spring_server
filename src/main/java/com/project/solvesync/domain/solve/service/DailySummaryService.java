package com.project.solvesync.domain.solve.service;

import com.project.solvesync.domain.solve.dto.DailySummaryDtos;
import com.project.solvesync.domain.solve.mq.DailySummaryUpsertMessage;

import java.time.LocalDate;
import java.util.List;

public interface DailySummaryService {

    /** (HTTP) 배치 요청을 MQ에 적재한다 */
    int publishBatch(DailySummaryDtos.UpsertBatchRequest request);

    /** (MQ Consumer) 메시지를 DB에 upsert 한다 */
    void upsertFromMessage(DailySummaryUpsertMessage message);

    /** (Dev) 저장된 요약 조회 */
    List<DailySummaryDtos.SummaryView> getSummaries(Long userPlatformAccountId, LocalDate from, LocalDate to);
}
