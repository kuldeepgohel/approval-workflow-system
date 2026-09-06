package com.kd.aws.dto.request;

import lombok.Data;

@Data
public class UpdateRequestRequest {

    private String title;

    private String description;

    private String requestedBy;

    private Long departmentId;
}
