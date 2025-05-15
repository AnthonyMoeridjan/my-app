package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Timesheet;
import com.cofeecode.application.powerhauscore.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TimesheetService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    // Retrieve all timesheets
    public List<Timesheet> findAll() {
        return timesheetRepository.findAll();
    }

    // Find a single timesheet by ID (needed for editing)
    public Optional<Timesheet> findById(Long id) {
        return timesheetRepository.findById(id);
    }

    // Save or update a timesheet
    public Timesheet save(Timesheet timesheet) {
        if (timesheet.getWorker() != null && timesheet.getWorker().getUurloon() != null) {
            BigDecimal uurloon = timesheet.getWorker().getUurloon();
            double hoursWorked = timesheet.getHoursWorked();

            BigDecimal earnings = calculateEarnings(hoursWorked, uurloon);
            timesheet.setEarnings(earnings);
        } else {
            timesheet.setEarnings(BigDecimal.ZERO);
        }

        return timesheetRepository.save(timesheet);
    }

    private BigDecimal calculateEarnings(double hoursWorked, BigDecimal uurloon) {
        double normalHours = Math.min(hoursWorked, 9);  // First 9 hours
        double overtimeHours = Math.max(0, hoursWorked - 9); // Overtime hours

        BigDecimal normalPay = uurloon.multiply(BigDecimal.valueOf(normalHours));
        BigDecimal overtimePay = uurloon.multiply(BigDecimal.valueOf(overtimeHours)).multiply(BigDecimal.valueOf(1.5));

        return normalPay.add(overtimePay);
    }

    public void delete(Long id) {
        timesheetRepository.deleteById(id);
    }

    // OPTIONAL: Find all timesheets for a specific worker
    public List<Timesheet> findByWorkerId(Long workerId) {
        return timesheetRepository.findByWorkerId(workerId);
    }

    // OPTIONAL: Find all timesheets for a specific project
    public List<Timesheet> findByProjectId(Long projectId) {
        return timesheetRepository.findByProjectId(projectId);
    }
}
