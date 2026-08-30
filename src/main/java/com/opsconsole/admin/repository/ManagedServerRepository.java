package com.opsconsole.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.opsconsole.admin.domain.ManagedServer;
public interface ManagedServerRepository extends JpaRepository<ManagedServer, Long> {

    Optional<ManagedServer> findByName(String name);

    List<ManagedServer> findByEnabledTrueOrderByNameAsc();
}
