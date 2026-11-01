package com.kd.aws.repository;

import com.kd.aws.entity.Request;
import com.kd.aws.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request,Long> {

//    List<Request> findByStatus(RequestStatus status);
//
//    List<Request> findByDepartmentId(Long departmentId);

    List<Request> findByStatusAndCurrentLevel( RequestStatus status, Integer currentLevel);

    List<Request> findByWorkflowId(Long workflowId);

    List<Request> findByWorkflowIdAndStatusAndCurrentLevel( Long workflowId, RequestStatus status, Integer currentLevel );

    @Query("""
            SELECT r
            FROM Request r
            WHERE LOWER(r.requestedBy) = LOWER(:email)
                OR(
                    :isActive = true
                    AND r.department.id = :departmentId
                    AND EXISTS (
                        SELECT ws.id
                        FROM WorkflowStep ws
                        WHERE ws.workflow.id = r.workflow.id
                        AND ws.level = r.currentLevel
                        AND ws.role.id = :roleId
                    )
                )
            """)
    Page<Request> findAccessibleRequests(
            @Param("email") String email,
            @Param("isActive") boolean isActive,
            @Param("departmentId") Long departmentId,
            @Param("roleId") Long roleId,
            Pageable pageable
    );
}
