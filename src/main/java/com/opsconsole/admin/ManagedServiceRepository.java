package com.opsconsole.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagedServiceRepository extends JpaRepository<ManagedService, Long> {

    List<ManagedService> findByServer_IdAndEnabledTrueOrderByCategoryAscNameAsc(Long serverId);

    List<ManagedService> findByEnabledTrueOrderByCategoryAscNameAsc();

    Optional<ManagedService> findByIdAndEnabledTrue(Long id);
}
