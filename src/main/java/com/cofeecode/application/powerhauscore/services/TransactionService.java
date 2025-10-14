package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.repository.ProjectRepository;
import com.cofeecode.application.powerhauscore.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Optional<Transaction> get(Long id) {
        return transactionRepository.findById(id);
    }

    public Transaction update(Transaction entity) {
        return transactionRepository.save(entity);
    }

    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }

    public Page<Transaction> list(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }


    public Page<Transaction> list(Pageable pageable, Specification<Transaction> filter) {
        return transactionRepository.findAll(filter, pageable);
    }

    public int count() {
        return (int) transactionRepository.count();
    }
    public Transaction create(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Project> findAllProjects() {
        return transactionRepository.findAllProjects();
    }

    public List<Transaction> findAll(Specification<Transaction> spec) {
        return transactionRepository.findAll(spec);
    }
}
