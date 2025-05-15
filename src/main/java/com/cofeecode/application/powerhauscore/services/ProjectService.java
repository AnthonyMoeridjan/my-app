package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {


    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public Project create(Project project) {
        return repository.save(project);
    }

    public Project update(Project project) {
        return repository.save(project);
    }

    public Optional<Project> get(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Project> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public List<Project> findAll() {
        return repository.findAll();
    }
}
