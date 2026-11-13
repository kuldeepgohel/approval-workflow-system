
package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApprovalWorkflowRepositoryTest {

    @Autowired
    private ApprovalWorkflowRepository workflowRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department createDepartment() {

        Department department = new Department();
        department.setName("TEST_DEPARTMENT");
        department.setDescription("Department for workflow testing");

        return departmentRepository.save(department);
    }

    private ApprovalWorkflow createWorkflow() {

        Department department = createDepartment();

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("TEST_WORKFLOW");
        workflow.setDepartment(department);
        workflow.setActive(true);

        return workflowRepository.save(workflow);
    }

    @Test
    void shouldSaveWorkflowSuccessfully() {

        ApprovalWorkflow savedWorkflow = createWorkflow();

        assertNotNull(savedWorkflow.getId());
        assertEquals("TEST_WORKFLOW", savedWorkflow.getName());
        assertTrue(savedWorkflow.getActive());
        assertEquals(
                "TEST_DEPARTMENT",
                savedWorkflow.getDepartment().getName()
        );
    }

    @Test
    void shouldFindWorkflowById() {

        ApprovalWorkflow savedWorkflow = createWorkflow();

        Optional<ApprovalWorkflow> result =
                workflowRepository.findById(savedWorkflow.getId());

        assertTrue(result.isPresent());
        assertEquals("TEST_WORKFLOW", result.get().getName());
    }

    @Test
    void shouldUpdateWorkflowSuccessfully() {

        ApprovalWorkflow savedWorkflow = createWorkflow();

        savedWorkflow.setName("UPDATED_WORKFLOW");
        savedWorkflow.setActive(false);

        workflowRepository.save(savedWorkflow);

        ApprovalWorkflow updatedWorkflow =
                workflowRepository.findById(savedWorkflow.getId())
                        .orElseThrow();

        assertEquals("UPDATED_WORKFLOW", updatedWorkflow.getName());
        assertFalse(updatedWorkflow.getActive());
    }

    @Test
    void shouldFindActiveWorkflowByDepartmentId() {

        Department department = createDepartment();

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("ACTIVE_WORKFLOW");
        workflow.setDepartment(department);
        workflow.setActive(true);

        workflowRepository.save(workflow);

        Optional<ApprovalWorkflow> result =
                workflowRepository.findByDepartmentIdAndActiveTrue(
                        department.getId()
                );

        assertTrue(result.isPresent());
        assertEquals("ACTIVE_WORKFLOW", result.get().getName());
        assertTrue(result.get().getActive());
    }

    @Test
    void shouldNotFindInactiveWorkflowByDepartmentId() {

        Department department = createDepartment();

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("INACTIVE_WORKFLOW");
        workflow.setDepartment(department);
        workflow.setActive(false);

        workflowRepository.save(workflow);

        Optional<ApprovalWorkflow> result =
                workflowRepository.findByDepartmentIdAndActiveTrue(
                        department.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteWorkflowSuccessfully() {

        ApprovalWorkflow savedWorkflow = createWorkflow();

        Long workflowId = savedWorkflow.getId();

        workflowRepository.deleteById(workflowId);

        assertFalse(workflowRepository.existsById(workflowId));
    }
}
