package com.kd.aws.service;

import com.kd.aws.dto.ApprovalHistoryDTO;
import com.kd.aws.dto.request.ApprovalRequestDTO;
import com.kd.aws.dto.RequestDTO;
import com.kd.aws.dto.request.UpdateRequestRequest;
import com.kd.aws.entity.ApprovalHistory;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Request;
import com.kd.aws.enums.ApprovalAction;
import com.kd.aws.enums.RequestStatus;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.RequestMapper;
import com.kd.aws.repository.ApprovalHistoryRepository;
import com.kd.aws.repository.DepartmentRepository;
import com.kd.aws.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final DepartmentRepository departmentRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final RequestMapper requestMapper;
    public RequestDTO createRequest(RequestDTO requestDTO){

        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(()->
                        new ResourceNotFoundException("Department not found with id: "
                                + requestDTO.getDepartmentId()));
        Request request = requestMapper.mapToEntity(requestDTO);
        request.setDepartment(department);

        Request savedRequest = requestRepository.save(request);
        return requestMapper.mapToDTO(savedRequest);
    }

    public List<RequestDTO> getAllRequest(){
        return requestRepository.findAll()
                .stream()
                .map(requestMapper::mapToDTO)
                .toList();
    }

    public RequestDTO getRequestById(Long id){
        Request request = requestRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Request not found with id: "+id));
        return requestMapper.mapToDTO(request);
    }

    public RequestDTO updateRequest(Long id, UpdateRequestRequest requestDTO) {
        Request request = requestRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Request not found with id: "+id));


        if(requestDTO.getTitle() != null){
            request.setTitle(requestDTO.getTitle());
        }
        if(requestDTO.getRequestedBy() != null){
            request.setRequesterBy(requestDTO.getRequestedBy());
        }
        if(requestDTO.getDescription() != null){
            request.setDescription(requestDTO.getDescription());
        }
        if(requestDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: "
                            + requestDTO.getDepartmentId()));

            request.setDepartment(department);
        }

        Request updatedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(updatedRequest);
    }

    public void deleteRequest(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: "+ id));
        requestRepository.deleteById(id);
    }


    public RequestDTO approveRequest(Long requestId, ApprovalRequestDTO approvalRequestDTO){
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: "+ requestId));
        if (request.getStatus() != RequestStatus.PENDING){
            throw new IllegalStateException(
                    "Only pending requests can be approved or rejected."
            );
        }
        request.setStatus(RequestStatus.APPROVED);
        ApprovalHistory history = new ApprovalHistory();

        history.setRequest(request);
        history.setApprovedBy(approvalRequestDTO.getApprovedBy());
        history.setAction(ApprovalAction.APPROVED);
        history.setComments(approvalRequestDTO.getComments());

        approvalHistoryRepository.save(history);
        Request updatedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(updatedRequest);
    }

    public RequestDTO rejectRequest(Long requestId, ApprovalRequestDTO approvalRequestDTO){
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: "+ requestId));
        if (request.getStatus() != RequestStatus.PENDING){
            throw new IllegalStateException(
                    "Only pending requests can be approved or rejected."
            );
        }
        request.setStatus(RequestStatus.REJECTED);

        ApprovalHistory history = new ApprovalHistory();

        history.setRequest(request);
        history.setApprovedBy(approvalRequestDTO.getApprovedBy());
        history.setAction(ApprovalAction.REJECTED);
        history.setComments(approvalRequestDTO.getComments());

        approvalHistoryRepository.save(history);
        Request updatedRequest = requestRepository.save(request);

        return requestMapper.mapToDTO(updatedRequest);
    }

    public List<ApprovalHistoryDTO> getApprovalHistory(Long requestId){
        requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found with id: " + requestId
                ));
        return approvalHistoryRepository.findByRequestId(requestId)
                .stream()
                .map(this::mapToApprovalHistoryDTO)
                .toList();
    }

    private ApprovalHistoryDTO mapToApprovalHistoryDTO(ApprovalHistory approvalHistory) {
        ApprovalHistoryDTO approvalHistoryDTO = new ApprovalHistoryDTO();

        approvalHistoryDTO.setId(approvalHistory.getId());
        approvalHistoryDTO.setRequestId(approvalHistory.getRequest().getId());
        approvalHistoryDTO.setAction(approvalHistory.getAction());
        approvalHistoryDTO.setApprovedBy(approvalHistory.getApprovedBy());
        approvalHistoryDTO.setComments(approvalHistory.getComments());
        approvalHistoryDTO.setActionDate(approvalHistory.getActionDate());

        return approvalHistoryDTO;
    }
}
