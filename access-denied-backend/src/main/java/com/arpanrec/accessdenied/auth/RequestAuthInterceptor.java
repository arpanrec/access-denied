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
import com.arpanrec.accessdenied.models.AccessLog;
import com.arpanrec.accessdenied.services.AccessLogRepository;
import com.arpanrec.accessdenied.services.NamespaceService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
public class RequestAuthInterceptor extends OncePerRequestFilter {

    private final NamespaceService nameSpaceService;
    private final AuthenticationManager authenticationManager;
    private final AccessLogRepository accessLogRepository;

    public RequestAuthInterceptor(
            @Autowired AuthenticationManagerImpl authenticationManagerImpl,
            @Autowired AccessLogRepository accessLogRepository,
            @Autowired NamespaceService nameSpaceService) {
        this.authenticationManager = authenticationManagerImpl;
        this.accessLogRepository = accessLogRepository;
        this.nameSpaceService = nameSpaceService;
    }

    protected void doFilterInternal(
            @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AccessLog accessLog = new AccessLog(request);
        accessLogRepository.save(accessLog);
        AuthenticationImpl authentication = new AuthenticationImpl();
        setCredentials(request, authentication);
        Authentication authenticated = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        filterChain.doFilter(request, response);
    }

    private void setCredentials(@NotNull HttpServletRequest request, AuthenticationImpl authentication) {
        String namespaceFromHeader = request.getHeader(AccessDeniedConstants.NAMESPACE_HEADER);
        authentication.setNamespace(
                nameSpaceService.getOptional(namespaceFromHeader).orElse(null));
        Enumeration<String> allHeaderNames = request.getHeaderNames();
        while (allHeaderNames.hasMoreElements()) {
            String apiHeader = allHeaderNames.nextElement();
            authentication.getHeaders().put(apiHeader, request.getHeader(apiHeader));
        }

        Cookie[] allCookies = request.getCookies();
        if (allCookies != null) {
            for (Cookie cookie : allCookies) {
                authentication.getCookies().put(cookie.getName(), cookie.getValue());
            }
        }

        authentication.setOrigin(request.getRemoteAddr());
        authentication.setMethod(request.getMethod());
        authentication.setUri(request.getRequestURI());
        authentication.setQueryString(request.getQueryString());
    }
}
