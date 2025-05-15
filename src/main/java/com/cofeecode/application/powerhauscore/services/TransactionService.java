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

    private final TransactionRepository repository;
    private final ProjectRepository projectRepository;

    public TransactionService(TransactionRepository repository, ProjectRepository projectRepository) {
        this.repository = repository;
        this.projectRepository = projectRepository;
    }

    public void saveTransaction(Transaction transaction) {
        repository.save(transaction);
    }

    public Optional<Transaction> get(Long id) {
        return repository.findById(id);
    }

    public Transaction update(Transaction entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Transaction> list(Pageable pageable) {
        return repository.findAll(pageable);
    }


    public Page<Transaction> list(Pageable pageable, Specification<Transaction> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    public Transaction create(Transaction transaction) {
        return repository.save(transaction);
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    public List<Transaction> findAll(Specification<Transaction> spec) {
        return repository.findAll(spec);
    }
}
