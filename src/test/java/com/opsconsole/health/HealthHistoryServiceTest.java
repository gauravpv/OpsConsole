package com.opsconsole.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealthHistoryServiceTest {

    private HealthHistoryService history;

    @BeforeEach
    void setUp() {
        history = new HealthHistoryService();
    }

    @Test
    void chartLast24Hours_usesRecordedSnapshots() {
        Instant base = Instant.now().minusSeconds(300);
        history.record(4, 2, base);
        history.record(4, 3, base.plusSeconds(60));
        history.record(4, 4, base.plusSeconds(120));

        var chart = history.chartLast24Hours(4, 4);

        assertThat(chart.points()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(chart.maxY()).isEqualTo(4);
        assertThat(chart.currentUp()).isEqualTo(4);
        assertThat(chart.linePath()).contains("M");
        assertThat(chart.areaPath()).contains("Z");
    }

    @Test
    void chartLast24Hours_defaultsWhenEmpty() {
        var chart = history.chartLast24Hours(3, 2);

        assertThat(chart.points()).hasSize(1);
        assertThat(chart.points().getFirst().upCount()).isEqualTo(2);
        assertThat(chart.xAxisLabels()).hasSize(5);
    }
}
