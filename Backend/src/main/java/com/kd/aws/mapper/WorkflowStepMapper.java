package com.kd.aws.mapper;

import com.kd.aws.dto.response.WorkflowStepResponse;
import com.kd.aws.entity.WorkflowStep;
import org.springframework.stereotype.Component;

@Component
public class WorkflowStepMapper {

    public WorkflowStepResponse toResponse(WorkflowStep workflowStep) {

        return WorkflowStepResponse.builder()
                .id(workflowStep.getId())
                .level(workflowStep.getLevel())
                .roleId(workflowStep.getRole().getId())
                .roleName(workflowStep.getRole().getName())
                .build();
    }
}
