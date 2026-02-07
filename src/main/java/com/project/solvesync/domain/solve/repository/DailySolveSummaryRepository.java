package com.project.solvesync.domain.solve.repository;

import com.project.solvesync.domain.solve.entity.DailySolveSummary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySolveSummaryRepository extends JpaRepository<DailySolveSummary, Long> {

    Optional<DailySolveSummary> findByUserPlatformAccountIdAndDate(Long userPlatformAccountId, LocalDate date);

    List<DailySolveSummary> findAllByUserPlatformAccountIdAndDateBetweenOrderByDateAsc(
            Long userPlatformAccountId,
            LocalDate from,
            LocalDate to
    );

    /**
     * 특정 기간에 대해 여러 계정의 DailySolveSummary를 한 번에 가져오기.
     * tierMin/tierMax 필터를 적용할 수 있도록 difficultyStats까지 fetch 한다.
     */
    @EntityGraph(attributePaths = "difficultyStats")
    List<DailySolveSummary> findAllByUserPlatformAccountIdInAndDateBetween(
            List<Long> userPlatformAccountIds,
            LocalDate from,
            LocalDate to
    );
}
