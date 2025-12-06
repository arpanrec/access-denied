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

import com.arpanrec.accessdenied.AccessDeniedConstants;
import com.arpanrec.accessdenied.exceptions.NotInitializedException;
import com.arpanrec.accessdenied.models.ApiKey;
import com.arpanrec.accessdenied.models.Namespace;
import com.arpanrec.accessdenied.services.ApiKeyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.security.auth.Subject;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Data
public class AuthenticationImpl implements Authentication {

    @Serial
    private static final long serialVersionUID = -8620294545092862085L;

    private boolean authenticated = false;

    private String uri;

    private Map<String, String> headers = new HashMap<>();

    private ApiKey apiKey;

    private Map<String, String> cookies = new HashMap<>();

    private String queryString;

    private String method;

    private String origin;

    private Namespace namespace;

    public void loadAuthentication(final ApiKeyService apiKeyService) {
        String authString = null;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase(AccessDeniedConstants.API_KEY_HEADER)) {
                authString = header.getValue();
                break;
            }
        }

        if (authString == null || authString.isBlank()) {
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                if (cookie.getKey().equalsIgnoreCase(AccessDeniedConstants.API_KEY_HEADER)) {
                    authString = cookie.getValue();
                    break;
                }
            }
        }

        if (authString == null || authString.isBlank()) {
            return;
        }

        if (authString.toLowerCase().startsWith("bearer ")) {
            authString = authString.substring(7);
        }
        Jws<Claims> parsedJwt = Jwts.parser()
                .keyLocator(header -> {
                    try {
                        return apiKeyService.getJwtSecretKey(namespace);
                    } catch (NotInitializedException e) {
                        throw new RuntimeException("Unable to retrieve JWT secret key", e);
                    }
                })
                .build()
                .parseSignedClaims(authString);
        this.apiKey = apiKeyService.findByIdAndNamespace(
                UUID.fromString(parsedJwt.getHeader().getKeyId()), namespace);
        this.apiKey = apiKeyService.setLastUsedAt(this.apiKey);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (apiKey == null) {
            return new ArrayList<>();
        }
        return apiKey.getUser().getAuthorities();
    }

    @Override
    public Object getCredentials() {
        if (apiKey == null) {
            return null;
        }
        return this.apiKey.getUser().getPassword();
    }

    @Override
    public Object getDetails() {
        if (apiKey == null) {
            return null;
        }
        return apiKey.getUser();
    }

    @Override
    public Object getPrincipal() {
        if (apiKey == null) {
            return null;
        }
        return this.apiKey.getUser().getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        if (apiKey == null) {
            return null;
        }
        return this.apiKey.getUser().getUsername();
    }

    @Override
    public boolean implies(Subject subject) {
        return Authentication.super.implies(subject);
    }
}
