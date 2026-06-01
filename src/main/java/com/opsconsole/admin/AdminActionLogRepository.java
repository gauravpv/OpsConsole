package com.opsconsole.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    List<AdminActionLog> findTop20ByOrderByCreatedAtDesc();

    List<AdminActionLog> findTop20ByActionNotOrderByCreatedAtDesc(AdminAction action);

    void deleteByAction(AdminAction action);
}
