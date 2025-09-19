package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Invoice;
import com.cofeecode.application.powerhauscore.data.InvoiceStatus;
import com.cofeecode.application.powerhauscore.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    public Optional<Invoice> get(Long id) {
        return repository.findById(id);
    }

    public Invoice update(Invoice entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Invoice> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Invoice> list(Pageable pageable, Specification<Invoice> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public BigDecimal getTotalOpenInvoices() {
        return repository.findAll().stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.SENT || invoice.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOverdueInvoices() {
        return repository.findAll().stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getInvoiceAging() {
        Map<String, BigDecimal> agingMap = new LinkedHashMap<>();
        agingMap.put("0-30 days", BigDecimal.ZERO);
        agingMap.put("31-60 days", BigDecimal.ZERO);
        agingMap.put("61-90 days", BigDecimal.ZERO);
        agingMap.put("90+ days", BigDecimal.ZERO);

        repository.findAll().stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.SENT || invoice.getStatus() == InvoiceStatus.OVERDUE)
                .forEach(invoice -> {
                    long daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
                    if (daysOverdue <= 30) {
                        agingMap.put("0-30 days", agingMap.get("0-30 days").add(invoice.getAmount()));
                    } else if (daysOverdue <= 60) {
                        agingMap.put("31-60 days", agingMap.get("31-60 days").add(invoice.getAmount()));
                    } else if (daysOverdue <= 90) {
                        agingMap.put("61-90 days", agingMap.get("61-90 days").add(invoice.getAmount()));
                    } else {
                        agingMap.put("90+ days", agingMap.get("90+ days").add(invoice.getAmount()));
                    }
                });

        return agingMap;
    }

    public List<Invoice> getOverdueInvoices() {
        return repository.findAll().stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.OVERDUE)
                .collect(Collectors.toList());
    }

     public Invoice create(Invoice invoice) {
        return repository.save(invoice);
    }
}
