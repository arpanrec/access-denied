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

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OncePerRequestFilter oncePerRequestFilter;
    private final AuthenticationProvider authenticationProvider;

    private final String servletPath;

    public SecurityConfig(
            @Autowired RequestAuthInterceptor requestAuthInterceptor,
            @Autowired AuthenticationProviderImpl authenticationProviderImpl,
            @Value("${spring.mvc.servlet.path:/}") String servletPath) {
        this.oncePerRequestFilter = requestAuthInterceptor;
        this.servletPath = servletPath;
        this.authenticationProvider = authenticationProviderImpl;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        http.sessionManagement(
                sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authenticationProvider(authenticationProvider);

        http.addFilterAfter(oncePerRequestFilter, BasicAuthenticationFilter.class);

        log.info("Application servlet path: {}", servletPath);
        PathPatternRequestMatcher.Builder applicationMatcher =
                PathPatternRequestMatcher.withDefaults().basePath(servletPath);

        PathPatternRequestMatcher.Builder h2ConsoleMatcher =
                PathPatternRequestMatcher.withDefaults().basePath("/h2-console");

        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(h2ConsoleMatcher.matcher("/**"))
                .permitAll()
                .requestMatchers(
                        applicationMatcher.matcher("/error"),
                        applicationMatcher.matcher("/actuator/**"),
                        applicationMatcher.matcher("/api-docs/**"),
                        applicationMatcher.matcher("/api/v1/initialization/**"),
                        applicationMatcher.matcher("/api/v1/login/**"))
                .permitAll()
                .requestMatchers(applicationMatcher.matcher("/api/v1/users/**"))
                .hasAuthority("ROLE_root")
                .anyRequest()
                .permitAll());

        return http.build();
    }
}
