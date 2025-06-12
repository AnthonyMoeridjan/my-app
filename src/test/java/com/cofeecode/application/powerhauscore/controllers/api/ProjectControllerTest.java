package com.cofeecode.application.powerhauscore.controllers.api;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.dto.ProjectRequestDTO;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.security.JwtAuthenticationFilter;
import com.cofeecode.application.powerhauscore.security.JwtTokenProvider;
import com.cofeecode.application.powerhauscore.security.UserDetailsServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Remove csrf import if not used, or keep if you plan to enable CSRF selectively
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = ProjectController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            JwtAuthenticationFilter.class, JwtTokenProvider.class
        })
    }
    // If UserDetailsServiceImpl is not found or causes issues, you might need to
    // provide a @TestConfiguration with a mock bean for it, or ensure it's correctly picked up/excluded.
    // For now, @MockBean for UserDetailsServiceImpl is added below.
)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl; // Mock UserDetailsService for WebMvcTest context

    @Autowired
    private ObjectMapper objectMapper;

    private Project project1;
    private ProjectRequestDTO projectRequestDTO;

    @BeforeEach
    void setUp() {
        project1 = new Project();
        project1.setId(1L);
        project1.setName("Test Project 1");
        project1.setDescription("Description 1");
        project1.setStartDate(LocalDate.now());
        project1.setBudget(new BigDecimal("10000"));
        project1.setStatus("ACTIVE");
        project1.setClient("Client A");

        projectRequestDTO = new ProjectRequestDTO(
            "New Project", "New Desc", LocalDate.now().plusDays(1), null,
            new BigDecimal("5000"), "PLANNED", "Client B", null, null, false
        );
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "RVC", "HR", "ADMIN"})
    void getAllProjects_shouldReturnPageOfProjects() throws Exception {
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project1), PageRequest.of(0, 10), 1);
        when(projectService.list(any(Pageable.class))).thenReturn(projectPage);

        mockMvc.perform(get("/api/v1/projects?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(project1.getName()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "RVC", "HR", "ADMIN"})
    void getProjectById_whenProjectExists_shouldReturnProject() throws Exception {
        when(projectService.get(1L)).thenReturn(Optional.of(project1));

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(project1.getName()));
    }

    @Test
    @WithMoc
