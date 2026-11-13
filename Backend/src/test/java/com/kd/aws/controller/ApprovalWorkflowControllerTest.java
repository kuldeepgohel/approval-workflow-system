
package com.kd.aws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.aws.dto.request.CreateApprovalWorkflowRequest;
import com.kd.aws.dto.request.UpdateApprovalWorkflowRequest;
import com.kd.aws.dto.request.WorkflowStepRequest;
import com.kd.aws.dto.response.ApprovalWorkflowResponse;
import com.kd.aws.exception.GlobalExceptionHandler;
import com.kd.aws.service.ApprovalWorkflowService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowControllerTest {

    @Mock
    private ApprovalWorkflowService approvalWorkflowService;

    @InjectMocks
    private ApprovalWorkflowController approvalWorkflowController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(approvalWorkflowController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }


    // --------------------------------------------------
    // Common test data
    // --------------------------------------------------

    private ApprovalWorkflowResponse createWorkflowResponse() {

        return ApprovalWorkflowResponse.builder()
                .id(1L)
                .name("IT Approval")
                .departmentId(1L)
                .departmentName("IT")
                .active(true)
                .steps(List.of())
                .build();
    }


    private WorkflowStepRequest createWorkflowStepRequest() {

        WorkflowStepRequest step = new WorkflowStepRequest();

        step.setLevel(1);
        step.setRoleId(2L);

        return step;
    }


    // --------------------------------------------------
    // POST /api/workflows
    // --------------------------------------------------

    @Test
    void createWorkflow_shouldReturn201() throws Exception {

        CreateApprovalWorkflowRequest request =
                new CreateApprovalWorkflowRequest();

        request.setName("IT Approval");
        request.setDepartmentId(1L);
        request.setSteps(List.of(createWorkflowStepRequest()));

        when(approvalWorkflowService.createWorkFlow(
                any(CreateApprovalWorkflowRequest.class)
        )).thenReturn(createWorkflowResponse());

        mockMvc.perform(post("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT Approval"))
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.active").value(true));

        verify(approvalWorkflowService).createWorkFlow(
                any(CreateApprovalWorkflowRequest.class)
        );
    }


    @Test
    void createWorkflow_withoutName_shouldReturn400() throws Exception {

        CreateApprovalWorkflowRequest request =
                new CreateApprovalWorkflowRequest();

        request.setName("");
        request.setDepartmentId(1L);
        request.setSteps(List.of(createWorkflowStepRequest()));

        mockMvc.perform(post("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(approvalWorkflowService);
    }


    @Test
    void createWorkflow_withoutDepartmentId_shouldReturn400() throws Exception {

        CreateApprovalWorkflowRequest request =
                new CreateApprovalWorkflowRequest();

        request.setName("IT Approval");
        request.setDepartmentId(null);
        request.setSteps(List.of(createWorkflowStepRequest()));

        mockMvc.perform(post("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.departmentId").exists());

        verifyNoInteractions(approvalWorkflowService);
    }


    @Test
    void createWorkflow_withoutSteps_shouldReturn400() throws Exception {

        CreateApprovalWorkflowRequest request =
                new CreateApprovalWorkflowRequest();

        request.setName("IT Approval");
        request.setDepartmentId(1L);
        request.setSteps(List.of());

        mockMvc.perform(post("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.steps").exists());

        verifyNoInteractions(approvalWorkflowService);
    }


    // --------------------------------------------------
    // GET /api/workflows
    // --------------------------------------------------

    @Test
    void getAllWorkflows_shouldReturn200() throws Exception {

        when(approvalWorkflowService.getAllWorkFlows())
                .thenReturn(List.of(createWorkflowResponse()));

        mockMvc.perform(get("/api/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("IT Approval"));

        verify(approvalWorkflowService).getAllWorkFlows();
    }


    @Test
    void getAllWorkflows_whenEmpty_shouldReturnEmptyList() throws Exception {

        when(approvalWorkflowService.getAllWorkFlows())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(approvalWorkflowService).getAllWorkFlows();
    }


    // --------------------------------------------------
    // GET /api/workflows/{id}
    // --------------------------------------------------

    @Test
    void getWorkflowById_shouldReturn200() throws Exception {

        when(approvalWorkflowService.getWorkflowById(1L))
                .thenReturn(createWorkflowResponse());

        mockMvc.perform(get("/api/workflows/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT Approval"))
                .andExpect(jsonPath("$.departmentName").value("IT"));

        verify(approvalWorkflowService).getWorkflowById(1L);
    }


    // --------------------------------------------------
    // PUT /api/workflows/{id}
    // --------------------------------------------------

    @Test
    void updateWorkflow_shouldReturn200() throws Exception {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("Updated IT Approval");
        request.setSteps(List.of(createWorkflowStepRequest()));

        ApprovalWorkflowResponse response = createWorkflowResponse();

        response.setName("Updated IT Approval");

        when(approvalWorkflowService.updateWorkflow(
                eq(1L),
                any(UpdateApprovalWorkflowRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/workflows/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated IT Approval"));

        verify(approvalWorkflowService).updateWorkflow(
                eq(1L),
                any(UpdateApprovalWorkflowRequest.class)
        );
    }


    @Test
    void updateWorkflow_withoutName_shouldReturn400() throws Exception {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("");
        request.setSteps(List.of(createWorkflowStepRequest()));

        mockMvc.perform(put("/api/workflows/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(approvalWorkflowService);
    }


    @Test
    void updateWorkflow_withoutSteps_shouldReturn400() throws Exception {

        UpdateApprovalWorkflowRequest request =
                new UpdateApprovalWorkflowRequest();

        request.setName("Updated IT Approval");
        request.setSteps(List.of());

        mockMvc.perform(put("/api/workflows/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.steps").exists());

        verifyNoInteractions(approvalWorkflowService);
    }


    // --------------------------------------------------
    // DELETE /api/workflows/{id}
    // --------------------------------------------------

    @Test
    void deleteWorkflow_shouldReturn204() throws Exception {

        doNothing().when(approvalWorkflowService).deleteWorkflow(1L);

        mockMvc.perform(delete("/api/workflows/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(approvalWorkflowService).deleteWorkflow(1L);
    }

}
