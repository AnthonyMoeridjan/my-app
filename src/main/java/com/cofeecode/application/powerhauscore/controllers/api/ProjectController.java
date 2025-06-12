package com.cofeecode.application.powerhauscore.controllers.api;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.dto.ProjectRequestDTO;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import jakarta.annotation.security.RolesAllowed; // Ensure this import is present
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List; // Make sure this import is present

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Page<Project>> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectService.list(pageable);
        return ResponseEntity.ok(projects);
    }

    // New endpoint to get all projects for selection (e.g., dropdowns)
    @GetMapping("/all")
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<List<Project>> getAllProjectsForSelection() {
        List<Project> allProjects = projectService.findAll();
        return ResponseEntity.ok(allProjects);
    }

    @GetMapping("/{id}")
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequestDTO projectDTO) {
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        Project createdProject = projectService.create(project);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequestDTO projectDTO) {
        Project existingProject = projectService.get(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + id));

        BeanUtils.copyProperties(projectDTO, existingProject, "id");
        Project updatedProject = projectService.update(existingProject);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (!projectService.get(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
