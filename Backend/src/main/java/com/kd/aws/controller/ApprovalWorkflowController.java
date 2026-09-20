package com.kd.aws.controller;

import com.kd.aws.dto.request.CreateApprovalWorkflowRequest;
import com.kd.aws.dto.request.UpdateApprovalWorkflowRequest;
import com.kd.aws.dto.response.ApprovalWorkflowResponse;
import com.kd.aws.service.ApprovalWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class ApprovalWorkflowController {

    private final ApprovalWorkflowService approvalWorkflowService;

    public ApprovalWorkflowController(ApprovalWorkflowService approvalWorkflowService){
        this.approvalWorkflowService = approvalWorkflowService;
    }
    /**
     * Create a new approval workflow
     */
    @PostMapping
    public ResponseEntity<ApprovalWorkflowResponse> createWorkFlow(
            @Valid @RequestBody CreateApprovalWorkflowRequest request
            ) {
        ApprovalWorkflowResponse createWorkflow =
                approvalWorkflowService.createWorkFlow(request);
        return new ResponseEntity<>(createWorkflow, HttpStatus.CREATED);
    }
    /**
     * Get all approval workflows
     */
    @GetMapping
    public ResponseEntity<List<ApprovalWorkflowResponse>> getAllWorkFlows() {
        List<ApprovalWorkflowResponse> workflows =
                approvalWorkflowService.getAllWorkFlows();
        return ResponseEntity.ok(workflows);
    }
    /**
     * Get Approval workflow by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApprovalWorkflowResponse> getWorkFlowById(
            @PathVariable Long id
    ) {
        ApprovalWorkflowResponse workflow =
                approvalWorkflowService.getWorkflowById(id);
        return ResponseEntity.ok(workflow);
    }

    /**
     * Update an existing approval workflow
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApprovalWorkflowResponse> updateWorkflow (
            @PathVariable Long id,
            @Valid @RequestBody UpdateApprovalWorkflowRequest request
            ) {
        ApprovalWorkflowResponse updatedWorkflow =
                approvalWorkflowService.updateWorkflow(
                        id, request);
        return ResponseEntity.ok(updatedWorkflow);
    }

    /**
     * Delete approval workflow
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(
            @PathVariable Long id
    ) {
        approvalWorkflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }
}
