package com.kd.aws.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApprovalWorkflowResponse {

    private Long id;

    private String name;

    private Long departmentId;

    private String departmentName;

    private Boolean active;

    private List<WorkflowStepResponse> steps;
}
