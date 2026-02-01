package com.project.solvesync.domain.room.repository;

import com.project.solvesync.domain.room.entity.RoomStatus;
import com.project.solvesync.domain.room.entity.RoomVisibility;
import com.project.solvesync.domain.room.entity.StudyRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    boolean existsByInviteCode(String inviteCode);

    @Query("""
        select r
        from StudyRoom r
        where r.visibility = :visibility
          and r.listed = true
          and r.status = :status
          and (:keyword is null or :keyword = '' or lower(r.name) like lower(concat('%', :keyword, '%')))
        order by r.id desc
    """)
    List<StudyRoom> findPublicListedDraftRooms(
            @Param("visibility") RoomVisibility visibility,
            @Param("status") RoomStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
