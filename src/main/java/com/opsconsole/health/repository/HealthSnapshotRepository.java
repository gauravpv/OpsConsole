package com.opsconsole.health.repository;

import com.opsconsole.health.domain.HealthSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface HealthSnapshotRepository extends JpaRepository<HealthSnapshotEntity, Long> {

    List<HealthSnapshotEntity> findByRecordedAtAfterOrderByRecordedAtAsc(Instant after);

    void deleteByRecordedAtBefore(Instant before);
}
