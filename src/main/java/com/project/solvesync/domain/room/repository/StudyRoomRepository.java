package com.project.solvesync.domain.room.repository;

import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import com.project.solvesync.domain.room.entity.StudyRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    /** 평가 스케줄러에서 ACTIVE 룸을 로딩할 때, rule + rulePlatforms를 한 번에 fetch */
    @Query("""
        select distinct r
        from StudyRoom r
        join fetch r.rule rr
        left join fetch r.rulePlatforms rp
        where r.status = :status
    """)
    java.util.List<StudyRoom> findAllByStatusWithRuleAndPlatforms(@Param("status") RoomStatus status);

    @Query("""
        select distinct r
        from StudyRoom r
        left join fetch r.rule rr
        left join fetch r.rulePlatforms rp
        where r.id = :roomId
    """)
    java.util.Optional<StudyRoom> findWithRuleAndPlatformsById(@Param("roomId") Long roomId);

    @Query("""
        select r
        from StudyRoom r
        where r.visibility = :visibility
          and r.listed = true
          and r.status = :status
          and (:keyword is null or :keyword = '' or lower(r.name) like lower(concat('%', :keyword, '%')))
        order by r.id desc
    """)
    Page<StudyRoom> searchPublicRooms(
            @Param("visibility") RoomVisibility visibility,
            @Param("status") RoomStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
