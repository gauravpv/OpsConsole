package com.opsconsole.web;

import com.opsconsole.activity.ActivityFeedService;
import com.opsconsole.admin.AdminActionLabels;
import com.opsconsole.admin.AdminActionLog;
import com.opsconsole.admin.ServiceAdminService;
import com.opsconsole.health.SystemHealthMonitor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PageController {

    private static final int DASHBOARD_ACTIVITY_LIMIT = 20;
    private static final DateTimeFormatter ACTION_TIME_FMT = DateTimeFormatter
            .ofPattern("d MMM yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final SystemHealthMonitor healthMonitor;
    private final ActivityFeedService activityFeedService;
    private final ServiceAdminService serviceAdminService;

    public PageController(
            SystemHealthMonitor healthMonitor,
            ActivityFeedService activityFeedService,
            ServiceAdminService serviceAdminService
    ) {
        this.healthMonitor = healthMonitor;
        this.activityFeedService = activityFeedService;
        this.serviceAdminService = serviceAdminService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        healthMonitor.refreshIfStale();
        model.addAttribute("summary", healthMonitor.summary());
        model.addAttribute("servicesUpChart", healthMonitor.servicesUpChart());
        model.addAttribute("activities", activityFeedService.recent(DASHBOARD_ACTIVITY_LIMIT));
        return page("dashboard", NavPage.DASHBOARD, model);
    }

    @GetMapping("/api-tester")
    public String apiTester(Model model) {
        return page("api-tester", NavPage.API_TESTER, model);
    }

    @GetMapping("/admin")
    public String systemAdmin(
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long serviceId,
            Model model
    ) {
        List<ServiceAdminService.ServerView> servers = serviceAdminService.listServers();
        ServiceAdminService.ServiceDetailView selectedService = resolveSelectedService(servers, serverId, serviceId);
        List<AdminActionLog> recentActions = serviceAdminService.recentActions(20);
        model.addAttribute("servers", servers);
        model.addAttribute("selectedService", selectedService);
        model.addAttribute("selectedServiceId", selectedService != null ? selectedService.id() : null);
        model.addAttribute("recentActions", recentActions);
        model.addAttribute("actionTimeFmt", ACTION_TIME_FMT);
        model.addAttribute("actionLabels", AdminActionLabels.class);
        return page("system-admin", NavPage.ADMIN, model);
    }

    private ServiceAdminService.ServiceDetailView resolveSelectedService(
            List<ServiceAdminService.ServerView> servers,
            Long serverId,
            Long serviceId
    ) {
        if (servers.isEmpty()) {
            return null;
        }
        if (serviceId != null) {
            try {
                return serviceAdminService.getServiceDetail(serviceId);
            } catch (IllegalArgumentException ignored) {
                // fall through to default selection
            }
        }
        ServiceAdminService.ServerView server = servers.stream()
                .filter(s -> serverId == null || serverId.equals(s.id()))
                .findFirst()
                .orElse(servers.getFirst());
        if (server.services().isEmpty()) {
            return null;
        }
        return serviceAdminService.getServiceDetail(server.services().getFirst().id());
    }

    @GetMapping("/logs")
    public String logsWatch(Model model) {
        return page("logs-watch", NavPage.LOGS, model);
    }

    private String page(String view, NavPage nav, Model model) {
        model.addAttribute("activeNav", nav.id());
        return view;
    }
}
