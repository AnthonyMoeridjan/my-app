package com.cofeecode.application.powerhauscore.repository;

import com.cofeecode.application.powerhauscore.data.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Page<Worker> findByActiveTrue(Pageable pageable);
    Page<Worker> findByActiveFalse(Pageable pageable);

    Page<Worker> findAll(Pageable pageable); // Added this

}