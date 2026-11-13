
package com.kd.aws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.aws.dto.ApprovalHistoryDTO;
import com.kd.aws.dto.RequestDTO;
import com.kd.aws.dto.request.ApprovalRequestDTO;
import com.kd.aws.dto.request.UpdateRequestRequest;
import com.kd.aws.enums.RequestStatus;
import com.kd.aws.exception.GlobalExceptionHandler;
import com.kd.aws.service.RequestService;

import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    // --------------------------------------------------
    // Common test data
    // --------------------------------------------------

    private RequestDTO createRequestDTO() {

        RequestDTO dto = new RequestDTO();

        dto.setId(1L);
        dto.setTitle("Laptop Purchase");
        dto.setDescription("Request for a new laptop");
        dto.setRequestedBy("user@gmail.com");
        dto.setDepartmentId(1L);
        dto.setDepartmentName("IT");
        dto.setWorkflowId(1L);
        dto.setWorkflowName("IT Approval");
        dto.setCurrentLevel(1);
        dto.setStatus(RequestStatus.PENDING);

        return dto;
    }


    // --------------------------------------------------
    // GET /api/requests
    // --------------------------------------------------

    @Test
    void getAllRequests_shouldReturn200() throws Exception {

        Page<RequestDTO> responsePage = new PageImpl<>(
                List.of(createRequestDTO()),
                PageRequest.of(0, 10),
                1
        );

        when(requestService.getAllRequest(any(Pageable.class)))
                .thenReturn(responsePage);

        mockMvc.perform(get("/api/requests")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Laptop Purchase"));

        verify(requestService).getAllRequest(any(Pageable.class));
    }


    // --------------------------------------------------
    // GET /api/requests/{id}
    // --------------------------------------------------

    @Test
    void getRequestById_shouldReturn200() throws Exception {

        when(requestService.getRequestById(1L))
                .thenReturn(createRequestDTO());

        mockMvc.perform(get("/api/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Laptop Purchase"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(requestService).getRequestById(1L);
    }


    // --------------------------------------------------
    // POST /api/requests
    // --------------------------------------------------

    @Test
    void createRequest_shouldReturn201() throws Exception {

        RequestDTO request = createRequestDTO();
        request.setId(null);

        when(requestService.createRequest(any(RequestDTO.class)))
                .thenReturn(createRequestDTO());

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Laptop Purchase"));

        verify(requestService).createRequest(any(RequestDTO.class));
    }


    @Test
    void createRequest_withEmptyTitle_shouldReturn400() throws Exception {

        RequestDTO request = createRequestDTO();
        request.setTitle("");

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.title").exists());

        verifyNoInteractions(requestService);
    }


    @Test
    void createRequest_withoutDepartment_shouldReturn400() throws Exception {

        RequestDTO request = createRequestDTO();
        request.setDepartmentId(null);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.departmentId").exists());

        verifyNoInteractions(requestService);
    }


    @Test
    void createRequest_withoutWorkflow_shouldReturn400() throws Exception {

        RequestDTO request = createRequestDTO();
        request.setWorkflowId(null);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.workflowId").exists());

        verifyNoInteractions(requestService);
    }


    // --------------------------------------------------
    // PATCH /api/requests/{id}
    // --------------------------------------------------

    @Test
    void updateRequest_shouldReturn200() throws Exception {

        UpdateRequestRequest request = new UpdateRequestRequest();

        request.setTitle("Updated Laptop Request");
        request.setDescription("Updated description");

        RequestDTO response = createRequestDTO();
        response.setTitle("Updated Laptop Request");

        when(requestService.updateRequest(
                eq(1L),
                any(UpdateRequestRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Updated Laptop Request"));

        verify(requestService).updateRequest(
                eq(1L),
                any(UpdateRequestRequest.class)
        );
    }


    // --------------------------------------------------
    // DELETE /api/requests/{id}
    // --------------------------------------------------

    @Test
    void deleteRequest_shouldReturn204() throws Exception {

        doNothing().when(requestService).deleteRequest(1L);

        mockMvc.perform(delete("/api/requests/1"))
                .andExpect(status().isNoContent());

        verify(requestService).deleteRequest(1L);
    }


    // --------------------------------------------------
    // POST /api/requests/{id}/approve
    // --------------------------------------------------

    @Test
    void approveRequest_shouldReturn200() throws Exception {

        ApprovalRequestDTO request = new ApprovalRequestDTO();
        request.setComments("Approved");

        RequestDTO response = createRequestDTO();
        response.setCurrentLevel(2);

        when(requestService.approveRequest(
                eq(1L),
                any(ApprovalRequestDTO.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currentLevel").value(2));

        verify(requestService).approveRequest(
                eq(1L),
                any(ApprovalRequestDTO.class)
        );
    }


    // --------------------------------------------------
    // POST /api/requests/{id}/reject
    // --------------------------------------------------

    @Test
    void rejectRequest_shouldReturn200() throws Exception {

        ApprovalRequestDTO request = new ApprovalRequestDTO();
        request.setComments("Rejected due to budget constraints");

        RequestDTO response = createRequestDTO();
        response.setStatus(RequestStatus.REJECTED);

        when(requestService.rejectRequest(
                eq(1L),
                any(ApprovalRequestDTO.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(requestService).rejectRequest(
                eq(1L),
                any(ApprovalRequestDTO.class)
        );
    }


    // --------------------------------------------------
    // GET /api/requests/{id}/history
    // --------------------------------------------------

    @Test
    void getApprovalHistory_shouldReturn200() throws Exception {

        ApprovalHistoryDTO history = new ApprovalHistoryDTO();

        history.setId(1L);
        history.setRequestId(1L);
        history.setLevel(1);
        history.setComments("Approved");
        history.setActionBy("manager@gmail.com");

        when(requestService.getApprovalHistory(1L))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/api/requests/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requestId").value(1))
                .andExpect(jsonPath("$[0].comments").value("Approved"))
                .andExpect(jsonPath("$[0].actionBy")
                        .value("manager@gmail.com"));

        verify(requestService).getApprovalHistory(1L);
    }

}
