package com.kd.aws.repository;

import com.kd.aws.entity.Request;
import com.kd.aws.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
