package com.kd.aws.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateApprovalWorkflowRequest {

    @NotBlank(message = "Workflow name is required")
    @Size(max = 100, message = "Workflow name cannot exceed 100 characters")
    private String name;

    @NotEmpty(message = "At least one workflow step is required")
    @Valid
    private List<WorkflowStepRequest> steps;
}
