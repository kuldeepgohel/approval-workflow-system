
package com.kd.aws.service;

import com.kd.aws.dto.ApprovalHistoryDTO;
import com.kd.aws.dto.RequestDTO;
import com.kd.aws.dto.request.ApprovalRequestDTO;
import com.kd.aws.dto.request.UpdateRequestRequest;
import com.kd.aws.entity.*;
import com.kd.aws.enums.ApprovalAction;
import com.kd.aws.enums.RequestStatus;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.RequestMapper;
import com.kd.aws.repository.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ApprovalWorkflowRepository approvalWorkflowRepository;

    @Mock
    private ApprovalHistoryRepository approvalHistoryRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RequestService requestService;

    private Department department;
    private Role managerRole;
    private Role adminRole;
    private ApprovalWorkflow workflow;
    private Request request;
    private User manager;
    private User admin;
    private WorkflowStep firstStep;
    private WorkflowStep secondStep;
    private RequestDTO requestDTO;
    private ApprovalRequestDTO approvalRequestDTO;

    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String MANAGER_EMAIL = "manager@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";

    @BeforeEach
    void setUp() {

        department = new Department();
        department.setId(1L);
        department.setName("IT");

        managerRole = new Role();
        managerRole.setId(10L);
        managerRole.setName("Manager");

        adminRole = new Role();
        adminRole.setId(20L);
        adminRole.setName("Admin");

        workflow = new ApprovalWorkflow();
        workflow.setId(100L);
        workflow.setName("IT Approval Workflow");
        workflow.setDepartment(department);
        workflow.setActive(true);

        request = new Request();
        request.setId(500L);
        request.setTitle("Laptop Request");
        request.setDescription("New laptop required");
        request.setRequestedBy(OWNER_EMAIL);
        request.setDepartment(department);
        request.setWorkflow(workflow);
        request.setStatus(RequestStatus.PENDING);
        request.setCurrentLevel(1);

        manager = new User();
        manager.setId(30L);
        manager.setName("Manager User");
        manager.setEmail(MANAGER_EMAIL);
        manager.setDepartment(department);
        manager.setRole(managerRole);
        manager.setActive(true);

        admin = new User();
        admin.setId(40L);
        admin.setName("Admin User");
        admin.setEmail(ADMIN_EMAIL);
        admin.setDepartment(department);
        admin.setRole(adminRole);
        admin.setActive(true);

        firstStep = new WorkflowStep();
        firstStep.setId(200L);
        firstStep.setWorkflow(workflow);
        firstStep.setLevel(1);
        firstStep.setRole(managerRole);

        secondStep = new WorkflowStep();
        secondStep.setId(201L);
        secondStep.setWorkflow(workflow);
        secondStep.setLevel(2);
        secondStep.setRole(adminRole);

        requestDTO = new RequestDTO();
        requestDTO.setId(500L);
        requestDTO.setTitle("Laptop Request");
        requestDTO.setDescription("New laptop required");
        requestDTO.setRequestedBy(OWNER_EMAIL);
        requestDTO.setDepartmentId(1L);
        requestDTO.setDepartmentName("IT");
        requestDTO.setWorkflowId(100L);
        requestDTO.setWorkflowName("IT Approval Workflow");
        requestDTO.setCurrentLevel(1);
        requestDTO.setStatus(RequestStatus.PENDING);

        approvalRequestDTO = new ApprovalRequestDTO();
        approvalRequestDTO.setComments("Reviewed");

        authenticateAs(OWNER_EMAIL, "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ================================================
    // CREATE REQUEST TESTS
    // ================================================

    @Test
    void createRequest_shouldCreateRequestSuccessfully() {

        RequestDTO input = new RequestDTO();
        input.setTitle("Laptop Request");
        input.setDepartmentId(1L);
        input.setWorkflowId(100L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.of(workflow));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(firstStep, secondStep));

        when(requestMapper.mapToEntity(input))
                .thenReturn(new Request());

        when(requestRepository.save(any(Request.class)))
                .thenAnswer(invocation -> {
                    Request saved = invocation.getArgument(0);
                    saved.setId(500L);
                    return saved;
                });

        when(requestMapper.mapToDTO(any(Request.class)))
                .thenReturn(requestDTO);

        RequestDTO result = requestService.createRequest(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(500L);

        verify(requestRepository).save(argThat(saved ->
                OWNER_EMAIL.equals(saved.getRequestedBy())
                        && saved.getCurrentLevel() == 1
                        && saved.getStatus() == RequestStatus.PENDING
        ));
    }

    @Test
    void createRequest_shouldThrowExceptionWhenDepartmentNotFound() {

        RequestDTO input = new RequestDTO();
        input.setDepartmentId(1L);
        input.setWorkflowId(100L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.createRequest(input)
        );

        verify(approvalWorkflowRepository, never()).findById(any());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_shouldThrowExceptionWhenWorkflowNotFound() {

        RequestDTO input = new RequestDTO();
        input.setDepartmentId(1L);
        input.setWorkflowId(100L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.createRequest(input)
        );

        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_shouldThrowExceptionWhenWorkflowBelongsToAnotherDepartment() {

        Department anotherDepartment = new Department();
        anotherDepartment.setId(2L);
        anotherDepartment.setName("HR");

        ApprovalWorkflow anotherWorkflow = new ApprovalWorkflow();
        anotherWorkflow.setId(101L);
        anotherWorkflow.setDepartment(anotherDepartment);

        RequestDTO input = new RequestDTO();
        input.setDepartmentId(1L);
        input.setWorkflowId(101L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findById(101L))
                .thenReturn(Optional.of(anotherWorkflow));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.createRequest(input)
        );

        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_shouldThrowExceptionWhenWorkflowHasNoSteps() {

        RequestDTO input = new RequestDTO();
        input.setDepartmentId(1L);
        input.setWorkflowId(100L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(approvalWorkflowRepository.findById(100L))
                .thenReturn(Optional.of(workflow));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of());

        when(requestMapper.mapToEntity(input))
                .thenReturn(new Request());

        assertThrows(
                IllegalStateException.class,
                () -> requestService.createRequest(input)
        );

        verify(requestRepository, never()).save(any());
    }

    // ================================================
    // GET ALL REQUEST TESTS
    // ================================================

    @Test
    void getAllRequest_shouldReturnAllRequestsForAdmin() {

        authenticateAs(ADMIN_EMAIL, "ROLE_ADMIN");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Request> requestPage =
                new PageImpl<>(List.of(request));

        when(requestRepository.findAll(pageable))
                .thenReturn(requestPage);

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        Page<RequestDTO> result =
                requestService.getAllRequest(pageable);

        assertThat(result.getContent()).hasSize(1);

        verify(requestRepository).findAll(pageable);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getAllRequest_shouldReturnAccessibleRequestsForNormalUser() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Request> requestPage =
                new PageImpl<>(List.of(request));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        when(requestRepository.findAccessibleRequests(
                eq(MANAGER_EMAIL),
                eq(true),
                eq(1L),
                eq(10L),
                eq(pageable)
        )).thenReturn(requestPage);

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        Page<RequestDTO> result =
                requestService.getAllRequest(pageable);

        assertThat(result.getContent()).hasSize(1);

        verify(requestRepository).findAccessibleRequests(
                MANAGER_EMAIL, true, 1L, 10L, pageable
        );
    }

    @Test
    void getAllRequest_shouldThrowExceptionWhenUserNotFound() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.getAllRequest(pageable)
        );

        verify(requestRepository, never())
                .findAccessibleRequests(anyString(), anyBoolean(), any(), any(), any());
    }

    // ================================================
    // GET REQUEST BY ID TESTS
    // ================================================

    @Test
    void getRequestById_shouldAllowAdmin() {

        authenticateAs(ADMIN_EMAIL, "ROLE_ADMIN");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result = requestService.getRequestById(500L);

        assertThat(result).isNotNull();

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getRequestById_shouldAllowRequestOwner() {

        authenticateAs(OWNER_EMAIL, "ROLE_USER");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result = requestService.getRequestById(500L);

        assertThat(result).isNotNull();

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getRequestById_shouldAllowAssignedApprover() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result = requestService.getRequestById(500L);

        assertThat(result).isNotNull();
    }

    @Test
    void getRequestById_shouldDenyUserWithWrongRole() {

        authenticateAs("other@example.com", "ROLE_USER");

        User otherUser = new User();
        otherUser.setId(50L);
        otherUser.setEmail("other@example.com");
        otherUser.setDepartment(department);
        otherUser.setRole(adminRole);
        otherUser.setActive(true);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(otherUser));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        assertThrows(
                AccessDeniedException.class,
                () -> requestService.getRequestById(500L)
        );
    }

    @Test
    void getRequestById_shouldThrowExceptionWhenRequestNotFound() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.getRequestById(500L)
        );
    }

    // ================================================
    // UPDATE REQUEST TESTS
    // ================================================

    @Test
    void updateRequest_shouldUpdateRequestSuccessfully() {

        authenticateAs(OWNER_EMAIL, "ROLE_USER");

        UpdateRequestRequest updateDTO = new UpdateRequestRequest();
        updateDTO.setTitle("Updated Laptop Request");
        updateDTO.setDescription("Updated description");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(requestRepository.save(request))
                .thenReturn(request);

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result =
                requestService.updateRequest(500L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(request.getTitle()).isEqualTo("Updated Laptop Request");
        assertThat(request.getDescription()).isEqualTo("Updated description");
        assertThat(request.getRequestedBy()).isEqualTo(OWNER_EMAIL);
    }

    @Test
    void updateRequest_shouldThrowExceptionWhenRequestNotFound() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.updateRequest(
                        500L, new UpdateRequestRequest()
                )
        );
    }

    @Test
    void updateRequest_shouldDenyAnotherUser() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        assertThrows(
                AccessDeniedException.class,
                () -> requestService.updateRequest(
                        500L, new UpdateRequestRequest()
                )
        );

        verify(requestRepository, never()).save(any());
    }

    @Test
    void updateRequest_shouldRejectNonPendingRequest() {

        request.setStatus(RequestStatus.APPROVED);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.updateRequest(
                        500L, new UpdateRequestRequest()
                )
        );

        verify(requestRepository, never()).save(any());
    }

    @Test
    void updateRequest_shouldThrowExceptionWhenDepartmentNotFound() {

        UpdateRequestRequest updateDTO = new UpdateRequestRequest();
        updateDTO.setDepartmentId(2L);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(departmentRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.updateRequest(500L, updateDTO)
        );

        verify(requestRepository, never()).save(any());
    }

    @Test
    void updateRequest_shouldRejectDepartmentNotMatchingWorkflow() {

        Department anotherDepartment = new Department();
        anotherDepartment.setId(2L);
        anotherDepartment.setName("HR");

        UpdateRequestRequest updateDTO = new UpdateRequestRequest();
        updateDTO.setDepartmentId(2L);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(departmentRepository.findById(2L))
                .thenReturn(Optional.of(anotherDepartment));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.updateRequest(500L, updateDTO)
        );

        verify(requestRepository, never()).save(any());
    }

    // ================================================
    // DELETE REQUEST TESTS
    // ================================================

    @Test
    void deleteRequest_shouldDeleteRequestSuccessfully() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(approvalHistoryRepository.existsByRequestId(500L))
                .thenReturn(false);

        requestService.deleteRequest(500L);

        verify(requestRepository).delete(request);
    }

    @Test
    void deleteRequest_shouldThrowExceptionWhenRequestNotFound() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.deleteRequest(500L)
        );

        verify(requestRepository, never()).delete(any());
    }

    @Test
    void deleteRequest_shouldPreventDeletionWhenApprovalHistoryExists() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(approvalHistoryRepository.existsByRequestId(500L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> requestService.deleteRequest(500L)
        );

        verify(requestRepository, never()).delete(any());
    }

    // ================================================
    // APPROVE REQUEST TESTS
    // ================================================

    @Test
    void approveRequest_shouldMoveRequestToNextLevel() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(firstStep, secondStep));

        when(requestRepository.save(request))
                .thenReturn(request);

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result =
                requestService.approveRequest(500L, approvalRequestDTO);

        assertThat(result).isNotNull();
        assertThat(request.getCurrentLevel()).isEqualTo(2);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);

        verify(approvalHistoryRepository).save(argThat(history ->
                history.getAction() == ApprovalAction.APPROVED
                        && history.getLevel() == 1
                        && MANAGER_EMAIL.equals(history.getActionBy())
        ));
    }

    @Test
    void approveRequest_shouldMarkRequestApprovedAtFinalLevel() {

        authenticateAs("admin@example.com", "ROLE_Admin");

        request.setCurrentLevel(2);

        WorkflowStep finalStep = secondStep;

        User finalApprover = new User();
        finalApprover.setId(40L);
        finalApprover.setEmail("admin@example.com");
        finalApprover.setDepartment(department);
        finalApprover.setRole(adminRole);
        finalApprover.setActive(true);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 2))
                .thenReturn(Optional.of(finalStep));

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(finalApprover));

        when(workflowStepRepository.findByWorkflowIdOrderByLevelAsc(100L))
                .thenReturn(List.of(firstStep, secondStep));

        when(requestRepository.save(request))
                .thenReturn(request);

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result =
                requestService.approveRequest(500L, approvalRequestDTO);

        assertThat(result).isNotNull();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);

        verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
    }

    @Test
    void approveRequest_shouldThrowExceptionWhenRequestNotFound() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.approveRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    @Test
    void approveRequest_shouldRejectAlreadyProcessedRequest() {

        request.setStatus(RequestStatus.APPROVED);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.approveRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    @Test
    void approveRequest_shouldRejectInactiveApprover() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        manager.setActive(false);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.approveRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    @Test
    void approveRequest_shouldRejectApproverFromAnotherDepartment() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        Department anotherDepartment = new Department();
        anotherDepartment.setId(2L);

        manager.setDepartment(anotherDepartment);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.approveRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    @Test
    void approveRequest_shouldRejectApproverWithWrongRole() {

        authenticateAs("admin@example.com", "ROLE_Admin");

        User wrongRoleUser = new User();
        wrongRoleUser.setId(40L);
        wrongRoleUser.setEmail("admin@example.com");
        wrongRoleUser.setDepartment(department);
        wrongRoleUser.setRole(adminRole);
        wrongRoleUser.setActive(true);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(wrongRoleUser));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.approveRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    // ================================================
    // REJECT REQUEST TESTS
    // ================================================

    @Test
    void rejectRequest_shouldRejectRequestSuccessfully() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        when(requestRepository.save(request))
                .thenReturn(request);

        when(requestMapper.mapToDTO(request))
                .thenReturn(requestDTO);

        RequestDTO result =
                requestService.rejectRequest(500L, approvalRequestDTO);

        assertThat(result).isNotNull();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);

        verify(approvalHistoryRepository).save(argThat(history ->
                history.getAction() == ApprovalAction.REJECTED
                        && history.getLevel() == 1
                        && MANAGER_EMAIL.equals(history.getActionBy())
        ));
    }

    @Test
    void rejectRequest_shouldThrowExceptionWhenRequestNotFound() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.rejectRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    @Test
    void rejectRequest_shouldRejectAlreadyProcessedRequest() {

        request.setStatus(RequestStatus.REJECTED);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.rejectRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    @Test
    void rejectRequest_shouldRejectUserWithWrongRole() {

        authenticateAs("admin@example.com", "ROLE_Admin");

        User wrongRoleUser = new User();
        wrongRoleUser.setId(40L);
        wrongRoleUser.setEmail("admin@example.com");
        wrongRoleUser.setDepartment(department);
        wrongRoleUser.setRole(adminRole);
        wrongRoleUser.setActive(true);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(wrongRoleUser));

        assertThrows(
                IllegalStateException.class,
                () -> requestService.rejectRequest(500L, approvalRequestDTO)
        );

        verify(approvalHistoryRepository, never()).save(any());
    }

    // ================================================
    // APPROVAL HISTORY TESTS
    // ================================================

    @Test
    void getApprovalHistory_shouldAllowAdmin() {

        authenticateAs(ADMIN_EMAIL, "ROLE_ADMIN");

        ApprovalHistory history = createHistory();

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(approvalHistoryRepository.findByRequestIdOrderByActionDateAsc(500L))
                .thenReturn(List.of(history));

        List<ApprovalHistoryDTO> result =
                requestService.getApprovalHistory(500L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo(ApprovalAction.APPROVED);
        assertThat(result.get(0).getActionBy()).isEqualTo(MANAGER_EMAIL);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getApprovalHistory_shouldAllowRequestOwner() {

        authenticateAs(OWNER_EMAIL, "ROLE_USER");

        ApprovalHistory history = createHistory();

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(approvalHistoryRepository.findByRequestIdOrderByActionDateAsc(500L))
                .thenReturn(List.of(history));

        List<ApprovalHistoryDTO> result =
                requestService.getApprovalHistory(500L);

        assertThat(result).hasSize(1);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getApprovalHistory_shouldAllowAssignedApprover() {

        authenticateAs(MANAGER_EMAIL, "ROLE_Manager");

        ApprovalHistory history = createHistory();

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(userRepository.findByEmail(MANAGER_EMAIL))
                .thenReturn(Optional.of(manager));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        when(approvalHistoryRepository.findByRequestIdOrderByActionDateAsc(500L))
                .thenReturn(List.of(history));

        List<ApprovalHistoryDTO> result =
                requestService.getApprovalHistory(500L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getApprovalHistory_shouldDenyUnauthorizedUser() {

        authenticateAs("other@example.com", "ROLE_USER");

        User otherUser = new User();
        otherUser.setId(50L);
        otherUser.setEmail("other@example.com");
        otherUser.setDepartment(department);
        otherUser.setRole(adminRole);
        otherUser.setActive(true);

        when(requestRepository.findById(500L))
                .thenReturn(Optional.of(request));

        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(otherUser));

        when(workflowStepRepository.findByWorkflowIdAndLevel(100L, 1))
                .thenReturn(Optional.of(firstStep));

        assertThrows(
                AccessDeniedException.class,
                () -> requestService.getApprovalHistory(500L)
        );

        verify(approvalHistoryRepository, never())
                .findByRequestIdOrderByActionDateAsc(any());
    }

    @Test
    void getApprovalHistory_shouldThrowExceptionWhenRequestNotFound() {

        when(requestRepository.findById(500L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.getApprovalHistory(500L)
        );
    }

    // ================================================
    // TEST DATA HELPERS
    // ================================================

    private void authenticateAs(String email, String authority) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        "password",
                        List.of(new SimpleGrantedAuthority(authority))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private ApprovalHistory createHistory() {

        ApprovalHistory history = new ApprovalHistory();
        history.setId(700L);
        history.setRequest(request);
        history.setLevel(1);
        history.setAction(ApprovalAction.APPROVED);
        history.setActionBy(MANAGER_EMAIL);
        history.setComments("Reviewed");
        history.setActionDate(LocalDateTime.now());

        return history;
    }
}
