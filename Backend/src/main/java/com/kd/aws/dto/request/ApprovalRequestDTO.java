package com.kd.aws.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalRequestDTO {

    private String comments;
}
