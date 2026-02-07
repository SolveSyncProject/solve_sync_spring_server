package com.project.solvesync.domain.solve.repository;

import com.project.solvesync.domain.solve.entity.DailySolveSummary;
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
}
