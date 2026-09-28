package com.kd.aws.service;

import com.kd.aws.dto.ApprovalHistoryDTO;
import com.kd.aws.dto.request.ApprovalRequestDTO;
import com.kd.aws.dto.RequestDTO;
import com.kd.aws.dto.request.UpdateRequestRequest;
import com.kd.aws.entity.*;
import com.kd.aws.enums.ApprovalAction;
import com.kd.aws.enums.RequestStatus;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.RequestMapper;
import com.kd.aws.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final DepartmentRepository departmentRepository;
    private final ApprovalWorkflowRepository approvalWorkflowRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    /**
     * Create a new Approval Request
     * @param requestDTO
     * @return
     */
    @Transactional
    public RequestDTO createRequest(RequestDTO requestDTO){

        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(()->
                        new ResourceNotFoundException("Department not found with id: "
                                + requestDTO.getDepartmentId()));
        ApprovalWorkflow workflow = approvalWorkflowRepository
                .findById(requestDTO.getWorkflowId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Approval workflow not found with id: "
                                        + requestDTO.getWorkflowId()
                        )
                );
        if(!workflow.getDepartment().getId().equals(department.getId())) {
            throw new IllegalStateException(
                    "Approval workflow does not belong to the selected department."
            );
        }
        Request request = requestMapper.mapToEntity(requestDTO);
        request.setDepartment(department);
        request.setWorkflow(workflow);

        Integer firstLevel = getFirstWorkflowLevel(workflow.getId());
        request.setCurrentLevel(firstLevel);
        request.setStatus(RequestStatus.PENDING);

        Request savedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(savedRequest);
    }

    /**
     * Get All Request
     * @return
     */
    public List<RequestDTO> getAllRequest(){
        return requestRepository.findAll()
                .stream()
                .map(requestMapper::mapToDTO)
                .toList();
    }

    /**
     * Get Request By ID
     * @param id
     * @return
     */
    public RequestDTO getRequestById(Long id){
        Request request = requestRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Request not found with id: "+id));
        return requestMapper.mapToDTO(request);
    }

    /**
     * Update an existing request
     * @param id
     * @param requestDTO
     * @return
     */
    @Transactional
    public RequestDTO updateRequest(Long id, UpdateRequestRequest requestDTO) {
        Request request = requestRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Request not found with id: "+id));

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending request can be updated."
            );
        }
        if(requestDTO.getTitle() != null){
            request.setTitle(requestDTO.getTitle());
        }
        if(requestDTO.getRequestedBy() != null){
            request.setRequestedBy(requestDTO.getRequestedBy());
        }
        if(requestDTO.getDescription() != null){
            request.setDescription(requestDTO.getDescription());
        }
        if(requestDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: "
                            + requestDTO.getDepartmentId()));

            if(!request.getWorkflow().getDepartment().getId().equals(department.getId())) {
                throw new IllegalStateException(
                        "Selected department does not match the request workflow."
                );
            }

            request.setDepartment(department);
        }
        Request updatedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(updatedRequest);
    }

    /**
     * Delete Request
     * @param id
     */
    public void deleteRequest(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: "+ id));
        requestRepository.delete(request);
    }

    /**
     * Approve the current level of a request
     * @param requestId
     * @param approvalRequestDTO
     * @return
     */
    @Transactional
    public RequestDTO approveRequest(Long requestId, ApprovalRequestDTO approvalRequestDTO){
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: "+ requestId));
        if (request.getStatus() != RequestStatus.PENDING){
            throw new IllegalStateException(
                    "Only pending requests can be approved or rejected."
            );
        }
        Integer currentLevel = request.getCurrentLevel();

//        Find the workflow step for the current level.
        WorkflowStep currentStep = getWorkflowStep(
                request.getWorkflow().getId(),
                currentLevel
        );
//        Find the user who is trying to approve the request.
        User approver = getApprover(
                approvalRequestDTO.getActionBy()
        );
/**
 * Verify that the user's role matches the role
 * configured for the current workflow level.
 */
        validateApproverRole(approver, currentStep, request);
//        request.setStatus(RequestStatus.APPROVED);
        ApprovalHistory history = new ApprovalHistory();

        history.setRequest(request);
        history.setLevel(currentLevel);
        history.setActionBy(approvalRequestDTO.getActionBy());
        history.setAction(ApprovalAction.APPROVED);
        history.setComments(approvalRequestDTO.getComments());

        approvalHistoryRepository.save(history);

//        Find the next level
        Integer nextLevel = getNextWorkflowLevel(
                request.getWorkflow().getId(),
                currentLevel
        );

        if(nextLevel != null) {
            request.setCurrentLevel(nextLevel);
        } else {
            request.setStatus(RequestStatus.APPROVED);
        }

        Request updatedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(updatedRequest);
    }

    /**
     * Reject the Current level of a request.
     * @param requestId
     * @param approvalRequestDTO
     * @return
     */
    public RequestDTO rejectRequest(Long requestId, ApprovalRequestDTO approvalRequestDTO){
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: "+ requestId));
        if (request.getStatus() != RequestStatus.PENDING){
            throw new IllegalStateException(
                    "Only pending requests can be approved or rejected."
            );
        }
        Integer currentLevel = request.getCurrentLevel();

        WorkflowStep currentStep = getWorkflowStep(
                request.getWorkflow().getId(),
                currentLevel
        );
        User approver = getApprover(
                approvalRequestDTO.getActionBy()
        );
        validateApproverRole(approver, currentStep, request);

        ApprovalHistory history = new ApprovalHistory();

        history.setRequest(request);
        history.setLevel(currentLevel);
        history.setActionBy(approvalRequestDTO.getActionBy());
        history.setAction(ApprovalAction.REJECTED);
        history.setComments(approvalRequestDTO.getComments());

        approvalHistoryRepository.save(history);

        request.setStatus(RequestStatus.REJECTED);
        Request updatedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(updatedRequest);
    }

    /**
     * Get Complete approval history for a request.
     * @param requestId
     * @return
     */
    public List<ApprovalHistoryDTO> getApprovalHistory(Long requestId){
        requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found with id: " + requestId
                ));
        return approvalHistoryRepository
                .findByRequestIdOrderByActionDateAsc(requestId)
                .stream()
                .map(this::mapToApprovalHistoryDTO)
                .toList();
    }

    /**
     * Convert ApprovalHistory entity to DTO
     * @param approvalHistory
     * @return
     */
    private ApprovalHistoryDTO mapToApprovalHistoryDTO(ApprovalHistory approvalHistory) {
        ApprovalHistoryDTO approvalHistoryDTO = new ApprovalHistoryDTO();

        approvalHistoryDTO.setId(approvalHistory.getId());
        approvalHistoryDTO.setRequestId(approvalHistory.getRequest().getId());
        approvalHistoryDTO.setAction(approvalHistory.getAction());
        approvalHistoryDTO.setActionBy(approvalHistory.getActionBy());
        approvalHistoryDTO.setComments(approvalHistory.getComments());
        approvalHistoryDTO.setLevel(approvalHistory.getLevel());
        approvalHistoryDTO.setActionDate(approvalHistory.getActionDate());

        return approvalHistoryDTO;
    }

    /**
     * Get the first level from the workflow.
     * @param workflowId
     * @return
     */
    private Integer getFirstWorkflowLevel(
            Long workflowId
    ) {
        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowIdOrderByLevelAsc(workflowId);
        if (steps.isEmpty()){
            throw new IllegalStateException(
                    "Approval workflow does not contain any steps."
            );
        }
        return steps.getFirst().getLevel();
    }

    private Integer getNextWorkflowLevel(
        Long workflowId,
        Integer currentLevel
    ) {
        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowIdOrderByLevelAsc(workflowId);
        return steps.stream()
                .map(WorkflowStep::getLevel)
                .filter(level -> level > currentLevel)
                .findFirst()
                .orElse(null);
    }

    private WorkflowStep getWorkflowStep(
            Long workflowId,
            Integer level
    ) {
        return workflowStepRepository.
                findByWorkflowIdAndLevel(workflowId, level)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Workflow step not found for workflow id: "
                                        + workflowId +
                                        " and level: " + level
                        )
                );
    }

    private User getApprover(String email) {
        return userRepository.
                findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email:" + email
                        )
                );
    }

    private void validateApproverRole(
            User approver,
            WorkflowStep workflowStep,
            Request request
    ) {
        if(!Boolean.TRUE.equals(approver.getActive())) {
            throw new IllegalStateException(
                    "User is not active and cannot approve or reject requests."
            );
        }
        // Department check for requester / approver and request
        if(!approver.getDepartment().getId().equals(
                request.getDepartment().getId()
        )) {
            throw new IllegalStateException(
                    "User does not belong to the request department"
            );
        }
        if(!approver.getRole().getId().equals(
                workflowStep.getRole().getId()
        )){
            throw new IllegalStateException(
                    "User does not have the required role to approve or reject "
                            + "this request at level "
                            + workflowStep.getLevel()
            );
        }

    }
}
