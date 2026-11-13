
package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalHistory;
import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Request;
import com.kd.aws.enums.ApprovalAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApprovalHistoryRepositoryTest {

    @Autowired
    private ApprovalHistoryRepository historyRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ApprovalWorkflowRepository workflowRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --------------------------------------------------
    // SAVE
    // --------------------------------------------------

    @Test
    void shouldSaveApprovalHistory() {

        Request request = createRequest();

        ApprovalHistory history = createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "approver@example.com"
        );

        assertNotNull(history.getId());
        assertNotNull(history.getActionDate());
        assertEquals(ApprovalAction.APPROVED, history.getAction());
        assertEquals("approver@example.com", history.getActionBy());
        assertEquals(1, history.getLevel());
        assertEquals(request.getId(), history.getRequest().getId());
    }

    // --------------------------------------------------
    // FIND BY ID
    // --------------------------------------------------

    @Test
    void shouldFindApprovalHistoryById() {

        Request request = createRequest();

        ApprovalHistory history = createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "approver@example.com"
        );

        Optional<ApprovalHistory> result =
                historyRepository.findById(history.getId());

        assertTrue(result.isPresent());
        assertEquals(history.getId(), result.get().getId());
        assertEquals(ApprovalAction.APPROVED, result.get().getAction());
    }

    // --------------------------------------------------
    // FIND BY REQUEST ID ORDER BY ACTION DATE
    // --------------------------------------------------

    @Test
    void shouldFindHistoryByRequestIdInAscendingOrder() {

        Request request = createRequest();

        ApprovalHistory firstHistory = createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "first@example.com"
        );

        // Ensure the first record has an earlier timestamp.
        jdbcTemplate.update(
                "UPDATE approval_history SET action_date = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)),
                firstHistory.getId()
        );

        ApprovalHistory secondHistory = createHistory(
                request,
                ApprovalAction.REJECTED,
                2,
                "second@example.com"
        );

        List<ApprovalHistory> result =
                historyRepository.findByRequestIdOrderByActionDateAsc(
                        request.getId()
                );

        assertEquals(2, result.size());
        assertEquals(firstHistory.getId(), result.get(0).getId());
        assertEquals(secondHistory.getId(), result.get(1).getId());
    }

    // --------------------------------------------------
    // FIND BY REQUEST ID AND LEVEL
    // --------------------------------------------------

    @Test
    void shouldFindHistoryByRequestIdAndLevelInAscendingOrder() {

        Request request = createRequest();

        ApprovalHistory firstHistory = createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "first@example.com"
        );

        jdbcTemplate.update(
                "UPDATE approval_history SET action_date = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)),
                firstHistory.getId()
        );

        createHistory(
                request,
                ApprovalAction.REJECTED,
                1,
                "second@example.com"
        );

        createHistory(
                request,
                ApprovalAction.APPROVED,
                2,
                "third@example.com"
        );

        List<ApprovalHistory> result =
                historyRepository.findByRequestIdAndLevelOrderByActionDateAsc(
                        request.getId(),
                        1
                );

        assertEquals(2, result.size());
        assertEquals(firstHistory.getId(), result.get(0).getId());
        assertEquals("second@example.com", result.get(1).getActionBy());
    }

    // --------------------------------------------------
    // FIND LATEST HISTORY BY REQUEST ID AND LEVEL
    // --------------------------------------------------

    @Test
    void shouldFindLatestHistoryForRequestAndLevel() {

        Request request = createRequest();

        ApprovalHistory firstHistory = createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "first@example.com"
        );

        jdbcTemplate.update(
                "UPDATE approval_history SET action_date = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)),
                firstHistory.getId()
        );

        ApprovalHistory latestHistory = createHistory(
                request,
                ApprovalAction.REJECTED,
                1,
                "second@example.com"
        );

        Optional<ApprovalHistory> result =
                historyRepository.findFirstByRequestIdAndLevelOrderByActionDateDesc(
                        request.getId(),
                        1
                );

        assertTrue(result.isPresent());
        assertEquals(latestHistory.getId(), result.get().getId());
        assertEquals("second@example.com", result.get().getActionBy());
    }

    // --------------------------------------------------
    // CHECK HISTORY EXISTS
    // --------------------------------------------------

    @Test
    void shouldReturnTrueWhenHistoryExistsForRequest() {

        Request request = createRequest();

        createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "approver@example.com"
        );

        assertTrue(historyRepository.existsByRequestId(request.getId()));
    }

    // --------------------------------------------------
    // CHECK HISTORY DOES NOT EXIST
    // --------------------------------------------------

    @Test
    void shouldReturnFalseWhenHistoryDoesNotExistForRequest() {

        assertFalse(historyRepository.existsByRequestId(99999L));
    }

    // --------------------------------------------------
    // DELETE HISTORY
    // --------------------------------------------------

    @Test
    void shouldDeleteApprovalHistory() {

        Request request = createRequest();

        ApprovalHistory history = createHistory(
                request,
                ApprovalAction.APPROVED,
                1,
                "approver@example.com"
        );

        Long historyId = history.getId();

        historyRepository.delete(history);
        historyRepository.flush();

        assertFalse(historyRepository.existsById(historyId));
    }

    // --------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------

    private Request createRequest() {

        Department department = new Department();
        department.setName("IT Department");
        department.setDescription("IT Department for testing");
        department = departmentRepository.saveAndFlush(department);

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setName("IT Approval Workflow");
        workflow.setDepartment(department);
        workflow.setActive(true);
        workflow = workflowRepository.saveAndFlush(workflow);

        Request request = new Request();
        request.setTitle("Laptop Purchase Request");
        request.setDescription("Request for a new laptop");
        request.setRequestedBy("employee@example.com");
        request.setDepartment(department);
        request.setWorkflow(workflow);

        return requestRepository.saveAndFlush(request);
    }

    private ApprovalHistory createHistory(
            Request request,
            ApprovalAction action,
            Integer level,
            String actionBy
    ) {

        ApprovalHistory history = new ApprovalHistory();
        history.setRequest(request);
        history.setAction(action);
        history.setComments("Test approval comment");
        history.setActionBy(actionBy);
        history.setLevel(level);

        return historyRepository.saveAndFlush(history);
    }
}
