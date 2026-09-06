package com.kd.aws.mapper;

import com.kd.aws.dto.user.UserResponse;
import com.kd.aws.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .departmentId(user.getDepartment().getId())
                .departmentName(user.getDepartment().getName())
                .roleId(user.getRole().getId())
                .roleName(user.getRole().getName())
                .active(user.getActive())
                .build();
    }
}
