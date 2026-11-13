package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory,Long> {

    Page<ApprovalHistory> findByRequestIdOrderByActionDateAsc(Long requestId, Pageable pageable);

    Page<ApprovalHistory> findByRequestIdAndLevelOrderByActionDateAsc( Long requestId, Integer level, Pageable pageable);

    Optional<ApprovalHistory> findFirstByRequestIdAndLevelOrderByActionDateDesc(
            Long requestId, Integer level );

    boolean existsByRequestId(Long requestId);
}
