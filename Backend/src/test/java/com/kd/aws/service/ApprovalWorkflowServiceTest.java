
package com.kd.aws.service;

import com.kd.aws.dto.request.CreateApprovalWorkflowRequest;
import com.kd.aws.dto.request.UpdateApprovalWorkflowRequest;
import com.kd.aws.dto.request.WorkflowStepRequest;
import com.kd.aws.dto.response.ApprovalWorkflowResponse;
import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Role;
import com.kd.aws.entity.WorkflowStep;
import com.kd.aws.mapper.ApprovalWorkflowMapper;
import com.kd.aws.repository.ApprovalWorkflowRepository;
import com.kd.aws.repository.DepartmentRepository;
import com.kd.aws.repository.RoleRepository;
import com.kd.aws.repository.WorkflowStepRepository;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowServiceTest {

    @Mock
    private ApprovalWorkflowRepository approvalWorkflowRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ApprovalWorkflowMapper approvalWorkflowMapper;

    @InjectMocks
    private ApprovalWorkflowServiceImpl approvalWorkflowService;

    private Department department;
    private Role role;
    private ApprovalWorkflow workflow;
    private WorkflowStep workflowStep;
    private ApprovalWorkflowResponse workflowResponse;

    @BeforeEach
    void setUp() {

        department = new Department();
        department.setId(1L);
        department.setName("IT");

        role = new Role();
        role.setId(10L);
        role.setName("Manager");

        workflow = new ApprovalWorkflow();
        workflow.setId(100L);
        workflow.setName("IT Approval Workflow");
        workflow.setDepartment(department);
        workflow.setActive(true);

        workflowStep = new WorkflowStep();
        workflowStep.setId(200L);
        workflowStep.setWorkflow(workflow);
        workflowStep.setLevel(1);
        workflowStep.setRole(role);

        workflowResponse = ApprovalWorkflowResponse.builder()
                .id(100L)
                .name("IT Approval Workflow")
                .departmentId(1L)
                .departmentName("IT")
                .active(true)
                .steps(List.of())
                .build();
    }

    // ================================================
    // CREATE WORKFLOW TESTS
    // ================================================

    @Test
    void createWorkflow_shouldCreateWorkflowSuccessfully() {

        CreateApprovalWorkflowRequest request = createRequest();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        when(approvalWorkflowRepository.save(any(ApprovalWorkflow.class)))
                .thenAnswer(invocation -> {
                    ApprovalWorkflow saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        when(roleRepository.findById(10L))
                .thenReturn(Optional.of(role));

        when(workflowStepRepository.save(any(WorkflowStep.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(workflowStep));

        when(approvalWorkflowMapper.toResponse(any(ApprovalWorkflow.class), anyList()))
                .thenReturn(workflowResponse);

        ApprovalWorkflowResponse result =
                approvalWorkflowService.createWorkFlow(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("IT Approval Workflow");

        verify(approvalWorkflowRepository).save(any(ApprovalWorkflow.class));
        verify(workflowStepRepository).save(any(WorkflowStep.class));
        verify(approvalWorkflowMapper).toResponse(any(ApprovalWorkflow.class), anyList());
    }

    @Test
    void createWorkflow_shouldThrowExceptionWhenDepartmentNotFound() {

        CreateApprovalWorkflowRequest request = createRequest();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(approvalWorkflowRepository, never()).save(any());
    }

    @Test
    void createWorkflow_shouldThrowExceptionWhenActiveWorkflowAlreadyExists() {

        CreateApprovalWorkflowRequest request = createRequest();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(Optional.of(workflow));

        assertThrows(
                IllegalStateException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(approvalWorkflowRepository, never()).save(any());
    }

    @Test
    void createWorkflow_shouldThrowExceptionWhenRoleNotFound() {

        CreateApprovalWorkflowRequest request = createRequest();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        when(approvalWorkflowRepository.save(any(ApprovalWorkflow.class)))
                .thenReturn(workflow);

        when(roleRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(workflowStepRepository, never()).save(any());
    }

    @Test
    void createWorkflow_shouldThrowExceptionForDuplicateLevels() {

        CreateApprovalWorkflowRequest request = createRequest();

        request.setSteps(List.of(
                createStepRequest(1, 10L),
                createStepRequest(1, 10L)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(departmentRepository, never()).findById(any());
    }

    @Test
    void createWorkflow_shouldThrowExceptionWhenLevelsAreNotSequential() {

        CreateApprovalWorkflowRequest request = createRequest();

        request.setSteps(List.of(
                createStepRequest(2, 10L),
                createStepRequest(3, 10L)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(departmentRepository, never()).findById(any());
    }

    @Test
    void createWorkflow_shouldRejectSingleStepStartingAtLevelTwo() {

        CreateApprovalWorkflowRequest request = createRequest();

        request.setSteps(List.of(
                createStepRequest(2, 10L)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(departmentRepository, never()).findById(any());
    }

    @Test
    void createWorkflow_shouldRejectMissingIntermediateLevel() {

        CreateApprovalWorkflowRequest request = createRequest();

        request.setSteps(List.of(
                createStepRequest(1, 10L),
                createStepRequest(3, 10L)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalWorkflowService.createWorkFlow(request)
        );

        verify(departmentRepository, never()).findById(any());
    }

    // ================================================
    // GET ALL WORKFLOWS TESTS
    // ================================================

    @Test
    void getAllWorkflows_shouldReturnAllWorkflows() {

        when(approvalWorkflowRepository.findAll())
                .thenReturn(List.of(workflow));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(workflowStep));

        when(approvalWorkflowMapper.toResponse(workflow, List.of(workflowStep)))
                .thenReturn(workflowResponse);

        List<ApprovalWorkflowResponse> result =
                approvalWorkflowService.getAllWorkFlows();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);

        verify(approvalWorkflowRepository).findAll();
    }

    @Test
    void getAllWorkflows_shouldReturnEmptyListWhenNoWorkflowsExist() {

        when(approvalWorkflowRepository.findAll())
                .thenReturn(List.of());

        List<ApprovalWorkflowResponse> result =
                approvalWorkflowService.getAllWorkFlows();

        assertThat(result).isEmpty();

        verify(workflowStepRepository, never())
                .findByWorkflowIdOrderByLevelAsc(any());
    }

    // ================================================
    // GET WORKFLOW BY ID TESTS
    // ================================================

    @Test
    void getWorkflowById_shouldReturnWorkflowSuccessfully() {

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.of(workflow));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(workflowStep));

        when(approvalWorkflowMapper.toResponse(workflow, List.of(workflowStep)))
                .thenReturn(workflowResponse);

        ApprovalWorkflowResponse result =
                approvalWorkflowService.getWorkflowById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);

        verify(approvalWorkflowRepository).findById(100L);
    }

    @Test
    void getWorkflowById_shouldThrowExceptionWhenWorkflowNotFound() {

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> approvalWorkflowService.getWorkflowById(100L)
        );

        verify(workflowStepRepository, never())
                .findByWorkflowIdOrderByLevelAsc(any());
    }

    // ================================================
    // UPDATE WORKFLOW TESTS
    // ================================================

    @Test
    void updateWorkflow_shouldUpdateWorkflowSuccessfully() {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("Updated IT Workflow");
        request.setSteps(List.of(createStepRequest(1, 10L)));

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.of(workflow));

        when(approvalWorkflowRepository.save(any(ApprovalWorkflow.class)))
                .thenReturn(workflow);

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(workflowStep))
                .thenReturn(List.of(workflowStep));

        when(roleRepository.findById(10L))
                .thenReturn(Optional.of(role));

        when(workflowStepRepository.save(any(WorkflowStep.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(approvalWorkflowMapper.toResponse(any(ApprovalWorkflow.class), anyList()))
                .thenReturn(workflowResponse);

        ApprovalWorkflowResponse result =
                approvalWorkflowService.updateWorkflow(100L, request);

        assertThat(result).isNotNull();

        verify(approvalWorkflowRepository).save(workflow);
        verify(workflowStepRepository).deleteAll(List.of(workflowStep));
        verify(workflowStepRepository).save(any(WorkflowStep.class));
    }

    @Test
    void updateWorkflow_shouldThrowExceptionWhenWorkflowNotFound() {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("Updated Workflow");
        request.setSteps(List.of(createStepRequest(1, 10L)));

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> approvalWorkflowService.updateWorkflow(100L, request)
        );

        verify(approvalWorkflowRepository, never()).save(any());
    }

    @Test
    void updateWorkflow_shouldThrowExceptionWhenRoleNotFound() {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("Updated Workflow");
        request.setSteps(List.of(createStepRequest(1, 10L)));

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.of(workflow));

        when(approvalWorkflowRepository.save(any(ApprovalWorkflow.class)))
                .thenReturn(workflow);

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of());

        when(roleRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> approvalWorkflowService.updateWorkflow(100L, request)
        );

        verify(workflowStepRepository, never()).save(any());
    }

    @Test
    void updateWorkflow_shouldThrowExceptionForDuplicateLevels() {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("Updated Workflow");
        request.setSteps(List.of(
                createStepRequest(1, 10L),
                createStepRequest(1, 10L)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalWorkflowService.updateWorkflow(100L, request)
        );

        verify(approvalWorkflowRepository, never()).findById(any());
    }

    // ================================================
    // DELETE WORKFLOW TESTS
    // ================================================

    @Test
    void deleteWorkflow_shouldDeleteWorkflowAndItsSteps() {

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.of(workflow));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(workflowStep));

        approvalWorkflowService.deleteWorkflow(100L);

        verify(workflowStepRepository).deleteAll(List.of(workflowStep));
        verify(approvalWorkflowRepository).delete(workflow);
    }

    @Test
    void deleteWorkflow_shouldThrowExceptionWhenWorkflowNotFound() {

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> approvalWorkflowService.deleteWorkflow(100L)
        );

        verify(workflowStepRepository, never())
                .deleteAll(anyList());

        verify(approvalWorkflowRepository, never()).delete(any());
    }

    // ================================================
    // TEST DATA HELPERS
    // ================================================

    private CreateApprovalWorkflowRequest createRequest() {

        CreateApprovalWorkflowRequest request =
                new CreateApprovalWorkflowRequest();

        request.setName("IT Approval Workflow");
        request.setDepartmentId(1L);
        request.setSteps(List.of(createStepRequest(1, 10L)));

        return request;
    }

    private WorkflowStepRequest createStepRequest(Integer level, Long roleId) {

        WorkflowStepRequest request = new WorkflowStepRequest();
        request.setLevel(level);
        request.setRoleId(roleId);

        return request;
    }
}
