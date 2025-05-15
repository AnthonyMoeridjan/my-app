package com.cofeecode.application.powerhauscore.repository;

import com.cofeecode.application.powerhauscore.data.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {



}
