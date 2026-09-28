package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory,Long> {

    List<ApprovalHistory> findByRequestIdOrderByActionDateAsc(Long requestId);

    List<ApprovalHistory> findByRequestIdAndLevelOrderByActionDateAsc( Long requestId, Integer level );

    Optional<ApprovalHistory> findFirstByRequestIdAndLevelOrderByActionDateDesc(
            Long requestId, Integer level );
}
