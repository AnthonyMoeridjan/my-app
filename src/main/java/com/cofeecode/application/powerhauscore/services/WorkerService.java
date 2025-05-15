package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Worker;
import com.cofeecode.application.powerhauscore.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    @Autowired
    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Worker create(Worker worker) {
        return workerRepository.save(worker);
    }

    public Worker update(Worker worker) {
        return workerRepository.save(worker);
    }

    public Optional<Worker> get(Long id) {
        return workerRepository.findById(id);
    }

    public void delete(Long id) {
        workerRepository.deleteById(id);
    }
    public Page<Worker> list(Pageable pageable) {
        return workerRepository.findAll(pageable);
    }

    public List<Worker> list(PageRequest pageRequest) {
        return workerRepository.findByActiveTrue(pageRequest).getContent(); // Show only active workers by default
    }

    public List<Worker> listAll(PageRequest pageRequest) {
        return workerRepository.findAll(pageRequest).getContent(); // Show all workers (active and inactive)
    }
    public List<Worker> findAll() {
        return workerRepository.findAll();
    }

    public void toggleActive(Long workerId) {
        workerRepository.findById(workerId).ifPresent(worker -> {
            worker.setActive(!worker.isActive());
            workerRepository.save(worker);
        });
    }

    public List<Worker> listActive(PageRequest pageRequest) {
        return workerRepository.findByActiveTrue(pageRequest).getContent();
    }

    public List<Worker> listInactive(PageRequest pageRequest) {
        return workerRepository.findByActiveFalse(pageRequest).getContent();
    }
}
