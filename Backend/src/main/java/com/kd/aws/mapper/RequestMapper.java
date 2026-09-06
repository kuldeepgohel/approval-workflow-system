package com.kd.aws.mapper;

import com.kd.aws.dto.RequestDTO;
import com.kd.aws.entity.Request;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestDTO mapToDTO(Request request) {

        RequestDTO dto = new RequestDTO();

        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setRequestedBy(request.getRequesterBy());

        dto.setDepartmentId(request.getDepartment().getId());
        dto.setDepartmentName(request.getDepartment().getName());

        dto.setStatus(request.getStatus());

        return dto;
    }

    public Request mapToEntity(RequestDTO requestDTO){
        Request request = new Request();

        request.setTitle(requestDTO.getTitle());
        request.setDescription(requestDTO.getDescription());
        request.setRequesterBy(requestDTO.getRequestedBy());

        return request;
    }
}
