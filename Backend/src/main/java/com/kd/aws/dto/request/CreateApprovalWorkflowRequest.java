package com.kd.aws.dto.request;

import com.kd.aws.entity.WorkflowStep;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateApprovalWorkflowRequest {

    @NotBlank(message = "Workflow name is required")
    @Size(max = 100, message = "Workflow name can not exceed 100 characters")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotEmpty(message = "At least one workflow step is required")
    @Valid
    private List<WorkflowStepRequest> steps;
}
