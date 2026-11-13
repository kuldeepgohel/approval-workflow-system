
package com.kd.aws.integration;

import com.kd.aws.entity.ApprovalHistory;
import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Request;
import com.kd.aws.entity.Role;
import com.kd.aws.entity.User;
import com.kd.aws.entity.WorkflowStep;
import com.kd.aws.enums.ApprovalAction;
import com.kd.aws.enums.RequestStatus;
import com.kd.aws.repository.ApprovalHistoryRepository;
import com.kd.aws.repository.ApprovalWorkflowRepository;
import com.kd.aws.repository.DepartmentRepository;
import com.kd.aws.repository.RequestRepository;
import com.kd.aws.repository.RoleRepository;
import com.kd.aws.repository.UserRepository;
import com.kd.aws.repository.WorkflowStepRepository;
import com.kd.aws.service.JwtService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApprovalWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApprovalWorkflowRepository workflowRepository;

    @Autowired
    private WorkflowStepRepository workflowStepRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ApprovalHistoryRepository approvalHistoryRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCompleteMultiLevelApprovalWorkflow() throws Exception {

        // -----------------------------------------
        // 1. Create Department
        // -----------------------------------------

        Department department = new Department();
        department.setName("Integration Test Department");
        department.setDescription("Department for integration testing");

        department = departmentRepository.saveAndFlush(department);

        // -----------------------------------------
        // 2. Create Roles
        // -----------------------------------------

        Role levelOneRole = new Role();
        levelOneRole.setName("LEVEL_ONE_APPROVER");

        levelOneRole = roleRepository.saveAndFlush(levelOneRole);

        Role levelTwoRole = new Role();
        levelTwoRole.setName("LEVEL_TWO_APPROVER");

        levelTwoRole = roleRepository.saveAndFlush(levelTwoRole);

        // -----------------------------------------
        // 3. Create Users
        // -----------------------------------------

        User levelOneApprover = createUser(
                "Level One Approver",
                "level1.integration@example.com",
                department,
                levelOneRole
        );

        User levelTwoApprover = createUser(
                "Level Two Approver",
                "level2.integration@example.com",
                department,
                levelTwoRole
        );

        // -----------------------------------------
        // 4. Create Approval Workflow
        // -----------------------------------------

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("Integration Test Workflow");
        workflow.setDepartment(department);
        workflow.setActive(true);

        workflow = workflowRepository.saveAndFlush(workflow);

        // -----------------------------------------
        // 5. Configure Workflow Steps
        // -----------------------------------------

        WorkflowStep levelOneStep = new WorkflowStep();
        levelOneStep.setWorkflow(workflow);
        levelOneStep.setLevel(1);
        levelOneStep.setRole(levelOneRole);

        workflowStepRepository.saveAndFlush(levelOneStep);

        WorkflowStep levelTwoStep = new WorkflowStep();
        levelTwoStep.setWorkflow(workflow);
        levelTwoStep.setLevel(2);
        levelTwoStep.setRole(levelTwoRole);

        workflowStepRepository.saveAndFlush(levelTwoStep);

        // -----------------------------------------
        // 6. Create Request
        // -----------------------------------------

        Request request = new Request();
        request.setTitle("Integration Test Laptop Request");
        request.setDescription("Request for a new laptop");
        request.setRequestedBy("employee.integration@example.com");
        request.setDepartment(department);
        request.setWorkflow(workflow);
        request.setCurrentLevel(1);
        request.setStatus(RequestStatus.PENDING);

        request = requestRepository.saveAndFlush(request);

        Long requestId = request.getId();

        // -----------------------------------------
        // 7. Generate Level One JWT
        // -----------------------------------------

        String levelOneToken = generateToken(
                levelOneApprover.getEmail()
        );

        // -----------------------------------------
        // 8. Level One Approval
        // -----------------------------------------

        mockMvc.perform(
                post("/api/requests/{id}/approve", requestId)
                        .header(
                                "Authorization",
                                "Bearer " + levelOneToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "actionBy": "level1.integration@example.com",
                                    "comments": "Approved at level one."
                                }
                                """)
        ).andExpect(status().isOk());

        // Verify request moves to level two.
        entityManager.flush();
        entityManager.clear();

        Request afterLevelOne =
                requestRepository.findById(requestId).orElseThrow();

        assertEquals(RequestStatus.PENDING, afterLevelOne.getStatus());
        assertEquals(2, afterLevelOne.getCurrentLevel());

        // Verify first approval history.
        assertEquals(
                1,
                approvalHistoryRepository.findByRequestIdOrderByActionDateAsc(
                        requestId
                ).size()
        );

        // -----------------------------------------
        // 9. Generate Level Two JWT
        // -----------------------------------------

        String levelTwoToken = generateToken(
                levelTwoApprover.getEmail()
        );

        // -----------------------------------------
        // 10. Level Two Approval
        // -----------------------------------------

        mockMvc.perform(
                post("/api/requests/{id}/approve", requestId)
                        .header(
                                "Authorization",
                                "Bearer " + levelTwoToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "actionBy": "level2.integration@example.com",
                                    "comments": "Approved at level two."
                                }
                                """)
        ).andExpect(status().isOk());

        // -----------------------------------------
        // 11. Verify Final Request Status
        // -----------------------------------------
        entityManager.flush();
        entityManager.clear();

        Request finalRequest =
                requestRepository.findById(requestId).orElseThrow();

        assertEquals(RequestStatus.APPROVED, finalRequest.getStatus());
        assertEquals(2, finalRequest.getCurrentLevel());

        // -----------------------------------------
        // 12. Verify Approval History
        // -----------------------------------------

        var history =
                approvalHistoryRepository.findByRequestIdOrderByActionDateAsc(
                        requestId
                );

        assertEquals(2, history.size());

        assertEquals(
                ApprovalAction.APPROVED,
                history.get(0).getAction()
        );

        assertEquals(
                "level1.integration@example.com",
                history.get(0).getActionBy()
        );

        assertEquals(
                ApprovalAction.APPROVED,
                history.get(1).getAction()
        );

        assertEquals(
                "level2.integration@example.com",
                history.get(1).getActionBy()
        );
    }

    // -----------------------------------------
    // HELPER: CREATE USER
    // -----------------------------------------

    private User createUser(
            String name,
            String email,
            Department department,
            Role role
    ) {

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("TestPassword@123"));
        user.setDepartment(department);
        user.setRole(role);
        user.setActive(true);

        return userRepository.saveAndFlush(user);
    }

    // -----------------------------------------
    // HELPER: GENERATE JWT
    // -----------------------------------------

    private String generateToken(String email) {

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        return jwtService.generateToken(userDetails);
    }
}
