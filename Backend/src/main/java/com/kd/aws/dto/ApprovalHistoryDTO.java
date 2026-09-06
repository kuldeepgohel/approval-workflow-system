package com.kd.aws.dto;

import com.kd.aws.enums.ApprovalAction;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalHistoryDTO {

    private Long id;

    private Long requestId;

    private ApprovalAction action;

    private String comments;

    private String approvedBy;

    private LocalDateTime actionDate;
}
