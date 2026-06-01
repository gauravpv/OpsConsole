package com.opsconsole.health;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoredHostRepository extends JpaRepository<MonitoredHost, Long> {

    List<MonitoredHost> findByEnabledTrueOrderByNameAsc();

    boolean existsByHostAndPort(String host, int port);
}
