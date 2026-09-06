package com.kd.aws.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRequestRequest {
    @NotBlank(message = "Title is required.")
    private String title;

    private String description;

    @NotBlank(message = "Requester name is required.")
    private String requestedBy;

    @NotNull(message = "Department is required.")
    private Long departmentId;
}
