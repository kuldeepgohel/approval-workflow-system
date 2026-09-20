package com.kd.aws.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowStepResponse {

    private Long id;

    private Integer level;

    private Long roleId;

    private String roleName;
}
