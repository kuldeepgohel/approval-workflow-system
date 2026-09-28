package com.kd.aws.dto;

import com.kd.aws.enums.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RequestDTO {

    private Long id;

    @NotBlank(message = "Title is required.")
    private String title;

    private String description;

    @NotBlank(message = "Requester name is required.")
    private String requestedBy;

    @NotNull(message = "Department is required.")
    private Long departmentId;

    private String departmentName;

    @NotNull(message = "Approval workflow is required.")
    private Long workflowId;

    private String workflowName;

    private Integer currentLevel;

    private RequestStatus status;
}
