package com.kd.aws.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalRequestDTO {

    @NotBlank(message = "Approver email is required.")
    private String actionBy;

    private String comments;
}
