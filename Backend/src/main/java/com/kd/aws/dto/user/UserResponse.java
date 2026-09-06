package com.kd.aws.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private Long departmentId;

    private String departmentName;

    private Long roleId;

    private String roleName;

    private Boolean active;
}
