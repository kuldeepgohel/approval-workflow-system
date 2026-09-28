package com.kd.aws.controller;

import com.kd.aws.dto.ApprovalHistoryDTO;
import com.kd.aws.dto.RequestDTO;
import com.kd.aws.dto.request.ApprovalRequestDTO;
import com.kd.aws.dto.request.UpdateRequestRequest;
import com.kd.aws.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PostMapping
    public ResponseEntity<RequestDTO> createRequest(
            @Valid @RequestBody RequestDTO requestDTO) {

        RequestDTO response = requestService.createRequest(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RequestDTO> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestRequest requestDTO) {

        return ResponseEntity.ok(requestService.updateRequest(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {

        requestService.deleteRequest(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestDTO> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO approvalRequestDTO) {

        return ResponseEntity.ok(requestService.approveRequest(id, approvalRequestDTO));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestDTO> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO approvalRequestDTO) {

        return ResponseEntity.ok(requestService.rejectRequest(id, approvalRequestDTO));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ApprovalHistoryDTO>> getApprovalHistory(
            @PathVariable Long id) {

        return ResponseEntity.ok(requestService.getApprovalHistory(id));
    }
}