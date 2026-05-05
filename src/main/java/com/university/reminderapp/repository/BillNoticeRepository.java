package com.university.reminderapp.repository;


import com.university.reminderapp.model.BillNotice;
import com.university.reminderapp.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillNoticeRepository extends JpaRepository<BillNotice, Long> {
    List<BillNotice> findByUniversity(University university);

    @Query("SELECT bn FROM BillNotice bn WHERE bn.university.id = :universityId AND :currentDate BETWEEN bn.validFrom AND bn.validTo")
    List<BillNotice> findActiveNotices(@Param("universityId") Long universityId, @Param("currentDate") LocalDate currentDate);
}