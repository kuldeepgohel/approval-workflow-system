package com.kd.aws.dto;

import com.kd.aws.enums.ApprovalAction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalHistoryDTO {

    private Long id;

    private Long requestId;

    private Integer level;

    private ApprovalAction action;

    private String comments;

    private String actionBy;

    private LocalDateTime actionDate;
}
