package com.cofeecode.application.powerhauscore.repository;

import com.cofeecode.application.powerhauscore.data.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    // Find all timesheets for a given worker
    List<Timesheet> findByWorkerId(Long workerId);

    // Find all timesheets for a given project
    List<Timesheet> findByProjectId(Long projectId);

    // Retrieve all timesheets for a specific worker on a given date
    List<Timesheet> findByWorkerIdAndDate(Long workerId, LocalDate date);

    // Retrieve all timesheets for a specific project within a date range
    List<Timesheet> findByProjectIdAndDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);

}