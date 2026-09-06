package com.kd.aws.mapper;

import com.kd.aws.dto.DepartmentDTO;
import com.kd.aws.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDTO mapToDTO(Department department) {

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());

        return dto;
    }
}
