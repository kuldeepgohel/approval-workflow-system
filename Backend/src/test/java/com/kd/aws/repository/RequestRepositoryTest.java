
package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Request;
import com.kd.aws.enums.RequestStatus;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ApprovalWorkflowRepository workflowRepository;

    private ApprovalWorkflow createWorkflow() {

        Department department = new Department();
        department.setName("TEST_DEPARTMENT");
        department.setDescription("Department for request testing");

        department = departmentRepository.save(department);

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("TEST_WORKFLOW");
        workflow.setDepartment(department);
        workflow.setActive(true);

        return workflowRepository.save(workflow);
    }

    private Request createRequest(
            ApprovalWorkflow workflow,
            String title) {

        Request request = new Request();

        request.setTitle(title);
        request.setDescription("Test request description");
        request.setRequestedBy("test@example.com");
        request.setDepartment(workflow.getDepartment());
        request.setWorkflow(workflow);

        return requestRepository.save(request);
    }

    @Test
    void shouldSaveRequestSuccessfully() {

        ApprovalWorkflow workflow = createWorkflow();

        Request savedRequest =
                createRequest(workflow, "Test Request");

        assertNotNull(savedRequest.getId());
        assertEquals("Test Request", savedRequest.getTitle());
        assertEquals("test@example.com", savedRequest.getRequestedBy());
        assertEquals(RequestStatus.PENDING, savedRequest.getStatus());
        assertEquals(1, savedRequest.getCurrentLevel());
    }

    @Test
    void shouldFindRequestById() {

        ApprovalWorkflow workflow = createWorkflow();

        Request savedRequest =
                createRequest(workflow, "Find Request");

        Optional<Request> result =
                requestRepository.findById(savedRequest.getId());

        assertTrue(result.isPresent());
        assertEquals("Find Request", result.get().getTitle());
    }

    @Test
    void shouldFindRequestsByStatusAndCurrentLevel() {

        ApprovalWorkflow workflow = createWorkflow();

        createRequest(workflow, "Pending Request");

        List<Request> results =
                requestRepository.findByStatusAndCurrentLevel(
                        RequestStatus.PENDING,
                        1
                );

        assertFalse(results.isEmpty());

        assertTrue(results.stream().anyMatch(
                request -> request.getTitle().equals("Pending Request")
        ));
    }

    @Test
    void shouldFindRequestsByWorkflowId() {

        ApprovalWorkflow workflow = createWorkflow();

        createRequest(workflow, "Workflow Request");

        List<Request> results =
                requestRepository.findByWorkflowId(workflow.getId());

        assertFalse(results.isEmpty());

        assertTrue(results.stream().anyMatch(
                request -> request.getTitle().equals("Workflow Request")
        ));
    }

    @Test
    void shouldFindRequestsByWorkflowStatusAndLevel() {

        ApprovalWorkflow workflow = createWorkflow();

        createRequest(workflow, "Pending Workflow Request");

        List<Request> results =
                requestRepository.findByWorkflowIdAndStatusAndCurrentLevel(
                        workflow.getId(),
                        RequestStatus.PENDING,
                        1
                );

        assertFalse(results.isEmpty());

        assertTrue(results.stream().allMatch(
                request ->
                        request.getWorkflow().getId().equals(workflow.getId())
                                && request.getStatus() == RequestStatus.PENDING
                                && request.getCurrentLevel() == 1
        ));
    }

    @Test
    void shouldUpdateRequestSuccessfully() {

        ApprovalWorkflow workflow = createWorkflow();

        Request savedRequest =
                createRequest(workflow, "Old Request");

        savedRequest.setTitle("Updated Request");
        savedRequest.setDescription("Updated description");

        requestRepository.save(savedRequest);

        Request updatedRequest =
                requestRepository.findById(savedRequest.getId())
                        .orElseThrow();

        assertEquals("Updated Request", updatedRequest.getTitle());
        assertEquals(
                "Updated description",
                updatedRequest.getDescription()
        );
    }

    @Test
    void shouldDeleteRequestSuccessfully() {

        ApprovalWorkflow workflow = createWorkflow();

        Request savedRequest =
                createRequest(workflow, "Delete Request");

        Long requestId = savedRequest.getId();

        requestRepository.deleteById(requestId);

        assertFalse(requestRepository.existsById(requestId));
    }

    @Test
    void shouldReturnEmptyWhenRequestDoesNotExist() {

        Optional<Request> result =
                requestRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }
}
