package com.cofeecode.application.powerhauscore.repository;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.ProjectStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

List<Project> findByStatus(ProjectStatus status);

}
