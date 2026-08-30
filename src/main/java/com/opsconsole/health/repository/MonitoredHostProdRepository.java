package com.opsconsole.health.repository;

import com.opsconsole.health.domain.MonitoredHostProd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoredHostProdRepository extends JpaRepository<MonitoredHostProd, Long> {

    List<MonitoredHostProd> findByEnabledTrueOrderByNameAsc();

    List<MonitoredHostProd> findAllByOrderByNameAsc();

    boolean existsByHostAndPort(String host, int port);

    boolean existsByHostAndPortAndIdNot(String host, int port, Long id);
}
