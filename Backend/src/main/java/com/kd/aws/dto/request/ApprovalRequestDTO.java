package com.kd.aws.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalRequestDTO {

    @NotBlank(message = "Approver name is required.")
    private String approvedBy;

    private String comments;
}
