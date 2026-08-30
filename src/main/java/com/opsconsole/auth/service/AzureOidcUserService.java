package com.opsconsole.auth.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import com.opsconsole.auth.domain.AppUser;
import com.opsconsole.auth.domain.OpsOidcUser;
@Component
public class AzureOidcUserService extends OidcUserService {

    private final UserProvisioningService provisioningService;

    public AzureOidcUserService(UserProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        AppUser appUser = provisioningService.provisionFromOAuth(oidcUser);
        if (!appUser.isEnabled()) {
            throw new OAuth2AuthenticationException("Account is disabled");
        }
        return new OpsOidcUser(oidcUser, appUser);
    }
}
