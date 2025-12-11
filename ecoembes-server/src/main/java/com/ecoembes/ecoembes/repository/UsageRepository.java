package com.ecoembes.ecoembes.repository;

import com.ecoembes.ecoembes.domain.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageRepository extends JpaRepository<Usage, Long> {

    @Query("SELECT u FROM Usage u WHERE u.date BETWEEN :startDate AND :endDate ORDER BY u.date DESC")
    List<Usage> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Usage> findByDumpsterDumpsterId(String dumpsterId);
}

