package com.kd.aws.mapper;

import com.kd.aws.dto.response.ApprovalWorkflowResponse;
import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.WorkflowStep;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApprovalWorkflowMapper {

    private final WorkflowStepMapper workflowStepMapper;

    public ApprovalWorkflowMapper(WorkflowStepMapper workflowStepMapper){
        this.workflowStepMapper = workflowStepMapper;
    }
    public ApprovalWorkflowResponse toResponse(ApprovalWorkflow workflow, List<WorkflowStep> steps){
        return ApprovalWorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .departmentId(workflow.getDepartment().getId())
                .departmentName(workflow.getDepartment().getName())
                .active(workflow.getActive())
                .steps(
                        steps.stream()
                                .map(workflowStepMapper::toResponse)
                                .toList()
                ).build();
    }
}
