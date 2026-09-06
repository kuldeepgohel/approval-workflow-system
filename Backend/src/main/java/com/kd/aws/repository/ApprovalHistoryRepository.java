package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory,Long> {
    List<ApprovalHistory> findByRequestId(Long requestId);
}
