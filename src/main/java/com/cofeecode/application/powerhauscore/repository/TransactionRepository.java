package com.cofeecode.application.powerhauscore.repository;

import com.cofeecode.application.powerhauscore.data.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cofeecode.application.powerhauscore.data.Project;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT DISTINCT t.project FROM Transaction t WHERE t.project IS NOT NULL")
    List<Project> findAllProjects();

}
