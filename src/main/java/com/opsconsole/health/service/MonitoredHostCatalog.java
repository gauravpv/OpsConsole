package com.opsconsole.health.service;

import com.opsconsole.health.domain.HealthDeploymentTier;
import com.opsconsole.health.domain.MonitoredHost;
import com.opsconsole.health.domain.MonitoredHostProd;
import com.opsconsole.health.exception.MonitorRegistrationException;
import com.opsconsole.health.repository.MonitoredHostProdRepository;
import com.opsconsole.health.repository.MonitoredHostRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonitoredHostCatalog {

    private final MonitoredHostRepository uatRepository;
    private final MonitoredHostProdRepository prodRepository;

    public MonitoredHostCatalog(
            MonitoredHostRepository uatRepository,
            MonitoredHostProdRepository prodRepository
    ) {
        this.uatRepository = uatRepository;
        this.prodRepository = prodRepository;
    }

    public List<MonitoredHost> findEnabledUat() {
        return uatRepository.findByEnabledTrueOrderByNameAsc();
    }

    public List<MonitoredHostProd> findEnabledProd() {
        return prodRepository.findByEnabledTrueOrderByNameAsc();
    }

    public List<MonitoredHost> findAllUat() {
        return uatRepository.findAllByOrderByNameAsc();
    }

    public List<MonitoredHostProd> findAllProd() {
        return prodRepository.findAllByOrderByNameAsc();
    }

    public MonitoredHost saveUat(MonitoredHost host) {
        return uatRepository.save(host);
    }

    public MonitoredHostProd saveProd(MonitoredHostProd host) {
        return prodRepository.save(host);
    }

    public void delete(HealthDeploymentTier tier, Long id) {
        if (tier == HealthDeploymentTier.UAT) {
            uatRepository.delete(findUat(id));
            return;
        }
        prodRepository.delete(findProd(id));
    }

    public MonitoredHost findUat(Long id) {
        if (id == null) {
            throw new MonitorRegistrationException("Monitor id is required");
        }
        return uatRepository.findById(id)
                .orElseThrow(() -> new MonitorRegistrationException("System not found"));
    }

    public MonitoredHostProd findProd(Long id) {
        if (id == null) {
            throw new MonitorRegistrationException("Monitor id is required");
        }
        return prodRepository.findById(id)
                .orElseThrow(() -> new MonitorRegistrationException("System not found"));
    }

    public boolean existsByHostAndPort(HealthDeploymentTier tier, String host, int port, Long excludeId) {
        if (tier == HealthDeploymentTier.UAT) {
            return excludeId == null
                    ? uatRepository.existsByHostAndPort(host, port)
                    : uatRepository.existsByHostAndPortAndIdNot(host, port, excludeId);
        }
        return excludeId == null
                ? prodRepository.existsByHostAndPort(host, port)
                : prodRepository.existsByHostAndPortAndIdNot(host, port, excludeId);
    }
}
