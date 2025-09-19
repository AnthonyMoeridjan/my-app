package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Bill;
import com.cofeecode.application.powerhauscore.repository.BillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.cofeecode.application.powerhauscore.data.BillStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository repository;

    public BillService(BillRepository repository) {
        this.repository = repository;
    }

    public Optional<Bill> get(Long id) {
        return repository.findById(id);
    }

    public Bill update(Bill entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Bill> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Bill> list(Pageable pageable, Specification<Bill> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public BigDecimal getTotalOpenBills() {
        return repository.findAll().stream()
                .filter(bill -> bill.getStatus() == BillStatus.RECEIVED)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Bill create(Bill bill) {
        return repository.save(bill);
    }

    public List<Bill> getOverdueBills() {
        return repository.findAll().stream()
                .filter(bill -> bill.getStatus() == BillStatus.RECEIVED && bill.getDueDate().isBefore(java.time.LocalDate.now()))
                .collect(Collectors.toList());
    }
}
