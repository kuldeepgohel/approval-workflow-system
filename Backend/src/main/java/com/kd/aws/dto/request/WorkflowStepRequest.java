package com.kd.aws.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WorkflowStepRequest {
    @NotNull(message = "Level is required")
    private Integer level;

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
