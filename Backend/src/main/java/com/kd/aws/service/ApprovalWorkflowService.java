package com.kd.aws.service;

import com.kd.aws.dto.request.CreateApprovalWorkflowRequest;
import com.kd.aws.dto.request.UpdateApprovalWorkflowRequest;
import com.kd.aws.dto.response.ApprovalWorkflowResponse;

import java.util.List;

public interface ApprovalWorkflowService {

    ApprovalWorkflowResponse createWorkFlow(CreateApprovalWorkflowRequest request);

    List<ApprovalWorkflowResponse> getAllWorkFlows();

    ApprovalWorkflowResponse getWorkflowById(Long id);

    ApprovalWorkflowResponse updateWorkflow(
            Long id,
            UpdateApprovalWorkflowRequest request
    );

    void deleteWorkflow(Long id);
}
