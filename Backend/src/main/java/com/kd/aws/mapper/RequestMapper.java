package com.kd.aws.mapper;

import com.kd.aws.dto.RequestDTO;
import com.kd.aws.entity.Request;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    /**
     * Convert Request entity to RequestDTO
     * @param request
     * @return
     */
    public RequestDTO mapToDTO(Request request) {

        RequestDTO dto = new RequestDTO();

        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setRequestedBy(request.getRequestedBy());

        if(request.getDepartment() != null) {
            dto.setDepartmentId(request.getDepartment().getId());
            dto.setDepartmentName(request.getDepartment().getName());
        }

        if(request.getWorkflow() != null){
            dto.setWorkflowId(request.getWorkflow().getId());
            dto.setWorkflowName(request.getWorkflow().getName());
        }

        dto.setCurrentLevel(request.getCurrentLevel());

        dto.setStatus(request.getStatus());

        return dto;
    }

    /**
     * Convert RequestDTO to Request Entity
     * @param requestDTO
     * @return
     */
    public Request mapToEntity(RequestDTO requestDTO){
        Request request = new Request();

        request.setTitle(requestDTO.getTitle());
        request.setDescription(requestDTO.getDescription());
        request.setRequestedBy(requestDTO.getRequestedBy());

        return request;
    }
}
