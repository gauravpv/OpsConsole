package com.opsconsole.activity.repository;

import com.opsconsole.activity.domain.ActivityType;
import com.opsconsole.activity.domain.SystemActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemActivityLogRepository extends JpaRepository<SystemActivityLog, Long> {

    List<SystemActivityLog> findByOrderByCreatedAtDesc(Pageable pageable);

    List<SystemActivityLog> findByTypeOrderByCreatedAtDesc(ActivityType type, Pageable pageable);
}
