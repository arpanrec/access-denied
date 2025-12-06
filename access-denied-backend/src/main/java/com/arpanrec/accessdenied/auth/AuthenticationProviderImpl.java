/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.accessdenied.auth;

import com.arpanrec.accessdenied.exceptions.AuthenticationError;
import com.arpanrec.accessdenied.models.User;
import com.arpanrec.accessdenied.services.ApiKeyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AuthenticationProviderImpl implements AuthenticationProvider {

    private final ApiKeyService apiKeyService;

    public AuthenticationProviderImpl(@Autowired ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        AuthenticationImpl appAuthenticationImpl = (AuthenticationImpl) authentication;
        try {
            appAuthenticationImpl.loadAuthentication(apiKeyService);
        } catch (Exception e) {
            log.trace("Failed to load authentication", e);
            appAuthenticationImpl.setAuthenticated(false);
            return appAuthenticationImpl;
        }

        if (appAuthenticationImpl.getApiKey() == null) {
            appAuthenticationImpl.setAuthenticated(false);
            log.trace("No valid API key provided");
            return appAuthenticationImpl;
        }

        if (System.currentTimeMillis() > appAuthenticationImpl.getApiKey().getExpireAt()) {
            appAuthenticationImpl.setAuthenticated(false);
            throw new AuthenticationError("API key expired");
        }

        if (appAuthenticationImpl.getDetails() == null) {
            log.trace(
                    "User authentication set to false for {}, user details not provided.",
                    appAuthenticationImpl.getName());
            appAuthenticationImpl.setAuthenticated(false);
            return appAuthenticationImpl;
        }

        log.trace("User authentication started for {}", appAuthenticationImpl.getName());
        User user = (User) appAuthenticationImpl.getDetails();

        if (!user.isEnabled()
                || !user.isAccountNonExpired()
                || !user.isAccountNonLocked()
                || !user.isCredentialsNonExpired()) {
            appAuthenticationImpl.setAuthenticated(false);
            log.trace(
                    "User authentication set to false for {}, user disabled, expired, locked, credentials expired "
                            + "or password not provided.",
                    appAuthenticationImpl.getName());
            return appAuthenticationImpl;
        }

        appAuthenticationImpl.setAuthenticated(true);

        return appAuthenticationImpl;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
