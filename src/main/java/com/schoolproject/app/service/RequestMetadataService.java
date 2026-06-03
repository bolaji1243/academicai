package com.schoolproject.app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RequestMetadataService {

    public String getIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return trimToLength(forwardedFor.split(",")[0].trim(), 64);
        }

        return trimToLength(request.getRemoteAddr(), 64);
    }

    public String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        return trimToLength(request.getHeader("User-Agent"), 512);
    }

    private HttpServletRequest getCurrentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }

        return null;
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
