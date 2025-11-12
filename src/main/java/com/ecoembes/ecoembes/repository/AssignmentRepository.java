package com.ecoembes.ecoembes.repository;

import com.ecoembes.ecoembes.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByPlantPlantId(String plantId);
    List<Assignment> findByAssignmentDate(LocalDate date);
    List<Assignment> findByEmployeeEmployeeId(String employeeId);
}
