package com.opsconsole.auth.repository;

import com.opsconsole.auth.domain.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
}
