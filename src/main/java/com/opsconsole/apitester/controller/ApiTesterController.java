package com.opsconsole.apitester.controller;

import com.opsconsole.auth.domain.AppTab;
import com.opsconsole.auth.domain.AppUser;
import com.opsconsole.auth.domain.CurrentUser;
import com.opsconsole.auth.service.NavAccessService;
import com.opsconsole.config.OpsConsoleFeaturesProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.opsconsole.apitester.dto.ApiTesterProxyRequest;
import com.opsconsole.apitester.dto.ApiTesterProxyResponse;
import com.opsconsole.apitester.service.ApiTesterProxyService;
@RestController
@RequestMapping("/api/api-tester")
public class ApiTesterController {

    private final ApiTesterProxyService proxyService;
    private final NavAccessService navAccessService;
    private final OpsConsoleFeaturesProperties features;

    public ApiTesterController(
            ApiTesterProxyService proxyService,
            NavAccessService navAccessService,
            OpsConsoleFeaturesProperties features
    ) {
        this.proxyService = proxyService;
        this.navAccessService = navAccessService;
        this.features = features;
    }

    @PostMapping("/proxy")
    public ApiTesterProxyResponse proxy(@RequestBody ApiTesterProxyRequest request) {
        requireApiTesterAccess();
        return proxyService.execute(request);
    }

    private void requireApiTesterAccess() {
        if (!features.isApiTesterEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API Tester is disabled");
        }
        AppUser user = CurrentUser.requireUser();
        if (!navAccessService.canAccess(user, AppTab.API_TESTER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "API Tester access required");
        }
    }
}
