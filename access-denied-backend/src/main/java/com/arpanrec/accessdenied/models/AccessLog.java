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
package com.arpanrec.accessdenied.models;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity(name = "access_log_t")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccessLog {
    public AccessLog(@NotNull HttpServletRequest request) {
        originIp = request.getRemoteAddr();
        time = System.currentTimeMillis();
        Enumeration<String> allHeaderNames = request.getHeaderNames();
        while (allHeaderNames.hasMoreElements()) {
            String apiHeader = allHeaderNames.nextElement();
            headers.put(apiHeader, request.getHeader(apiHeader));
        }
        method = request.getMethod();
        uri = request.getRequestURI();

        for (String parameterKey : request.getParameterMap().keySet()) {
            queryString.put(parameterKey, request.getParameterValues(parameterKey));
        }

        Cookie[] allCookies = request.getCookies();
        if (allCookies != null) {
            for (Cookie cookie : allCookies) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
        }
        id = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "origin_ip_c")
    private String originIp;

    @Column(name = "time_c")
    private long time;

    @Column(name = "headers_c", columnDefinition = "TEXT")
    @Convert(converter = AccessLogMapAttributeConverter.class)
    private Map<String, String> headers = new HashMap<>();

    @Column(name = "cookies_c", columnDefinition = "TEXT")
    @Convert(converter = AccessLogMapAttributeConverter.class)
    private Map<String, String> cookies = new HashMap<>();

    @Column(name = "method_c")
    private String method;

    @Column(name = "uri_c")
    private String uri;

    @Column(name = "params_c")
    @Convert(converter = AccessLogQueryStringAttributeConverter.class)
    private Map<String, String[]> queryString = new HashMap<>();
}
