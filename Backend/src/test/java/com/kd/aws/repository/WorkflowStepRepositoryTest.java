
package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Role;
import com.kd.aws.entity.WorkflowStep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkflowStepRepositoryTest {

    @Autowired
    private WorkflowStepRepository workflowStepRepository;

    @Autowired
    private ApprovalWorkflowRepository workflowRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private ApprovalWorkflow createWorkflow() {

        Department department = new Department();
        department.setName("TEST_DEPARTMENT");
        department.setDescription("Department for testing");

        department = departmentRepository.save(department);

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("TEST_WORKFLOW");
        workflow.setDepartment(department);
        workflow.setActive(true);

        return workflowRepository.save(workflow);
    }

    private Role createRole(String name) {

        Role role = new Role();
        role.setName(name);

        return roleRepository.save(role);
    }

    private WorkflowStep createWorkflowStep(
            ApprovalWorkflow workflow,
            Role role,
            Integer level) {

        WorkflowStep step = new WorkflowStep();
        step.setWorkflow(workflow);
        step.setRole(role);
        step.setLevel(level);

        return workflowStepRepository.save(step);
    }

    @Test
    void shouldSaveWorkflowStepSuccessfully() {

        ApprovalWorkflow workflow = createWorkflow();
        Role role = createRole("MANAGER");

        WorkflowStep savedStep =
                createWorkflowStep(workflow, role, 1);

        assertNotNull(savedStep.getId());
        assertEquals(1, savedStep.getLevel());
        assertEquals("MANAGER", savedStep.getRole().getName());
    }

    @Test
    void shouldFindWorkflowStepById() {

        ApprovalWorkflow workflow = createWorkflow();
        Role role = createRole("MANAGER");

        WorkflowStep savedStep =
                createWorkflowStep(workflow, role, 1);

        Optional<WorkflowStep> result =
                workflowStepRepository.findById(savedStep.getId());

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getLevel());
    }

    @Test
    void shouldFindStepsByWorkflowIdInAscendingLevelOrder() {

        ApprovalWorkflow workflow = createWorkflow();

        Role managerRole = createRole("MANAGER");
        Role adminRole = createRole("ADMIN");
        Role directorRole = createRole("DIRECTOR");

        // Intentionally save steps in a different order
        createWorkflowStep(workflow, directorRole, 3);
        createWorkflowStep(workflow, managerRole, 1);
        createWorkflowStep(workflow, adminRole, 2);

        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowIdOrderByLevelAsc(
                        workflow.getId()
                );

        assertEquals(3, steps.size());

        assertEquals(1, steps.get(0).getLevel());
        assertEquals(2, steps.get(1).getLevel());
        assertEquals(3, steps.get(2).getLevel());
    }

    @Test
    void shouldFindWorkflowStepByWorkflowIdAndLevel() {

        ApprovalWorkflow workflow = createWorkflow();
        Role role = createRole("MANAGER");

        createWorkflowStep(workflow, role, 1);

        Optional<WorkflowStep> result =
                workflowStepRepository.findByWorkflowIdAndLevel(
                        workflow.getId(),
                        1
                );

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getLevel());
    }

    @Test
    void shouldReturnEmptyWhenWorkflowLevelDoesNotExist() {

        ApprovalWorkflow workflow = createWorkflow();

        Optional<WorkflowStep> result =
                workflowStepRepository.findByWorkflowIdAndLevel(
                        workflow.getId(),
                        99
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteWorkflowStepSuccessfully() {

        ApprovalWorkflow workflow = createWorkflow();
        Role role = createRole("MANAGER");

        WorkflowStep savedStep =
                createWorkflowStep(workflow, role, 1);

        Long stepId = savedStep.getId();

        workflowStepRepository.deleteById(stepId);

        assertFalse(workflowStepRepository.existsById(stepId));
    }
}
