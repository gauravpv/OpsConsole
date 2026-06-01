package com.opsconsole.activity;

import com.opsconsole.auth.AppUser;
import com.opsconsole.auth.AuthDataInitializer;
import com.opsconsole.health.HealthStatus;
import com.opsconsole.health.SystemHealthView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityFeedServiceTest {

    private ActivityFeedService feed;

    @BeforeEach
    void setUp() {
        feed = new ActivityFeedService();
    }

    @Test
    void recordLogin_addsEvent() {
        AppUser user = new AppUser("dev-admin", "admin@opsconsole.local", "Administrator", null);
        feed.recordLogin(user);

        assertThat(feed.recent(5)).hasSize(1);
        assertThat(feed.recent(1).getFirst().type()).isEqualTo(ActivityType.USER_LOGIN);
        assertThat(feed.recent(1).getFirst().messageHighlight()).isEqualTo("Administrator");
    }

    @Test
    void recordHealthStatusChange_onlyOnTransition() {
        SystemHealthView system = new SystemHealthView(
                1L, "API", "localhost", 8080, "UAT", "US-East",
                HealthStatus.DOWN, "DOWN", Instant.now(), 0, "timeout"
        );

        feed.recordHealthStatusChange(system, HealthStatus.UP, HealthStatus.DOWN);

        assertThat(feed.recent(5)).hasSize(1);
        assertThat(feed.recent(1).getFirst().type()).isEqualTo(ActivityType.HEALTH_DOWN);
    }

    @Test
    void recordUserRoleChanged_addsEvent() {
        AppUser actor = new AppUser("dev-admin", "admin@opsconsole.local", "Administrator", null);
        AppUser target = new AppUser("dev-tester", "tester@opsconsole.local", "Tester", null);

        feed.recordUserRoleChanged(actor, target, "Monitoring", "Tester");

        assertThat(feed.recent(1).getFirst().type()).isEqualTo(ActivityType.USER_ROLE_CHANGED);
        assertThat(feed.recent(1).getFirst().messageSuffix()).contains("Tester");
    }
}
