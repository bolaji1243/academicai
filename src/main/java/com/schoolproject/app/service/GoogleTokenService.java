package com.schoolproject.app.service;

import com.schoolproject.app.dto.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GoogleTokenService {

    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";

    private final String clientId;
    private final JwtDecoder jwtDecoder;

    public GoogleTokenService(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();
    }

    public GoogleUserInfo verify(String idToken) {
        if (clientId == null || clientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google login is not configured");
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
        }

        validateIssuer(jwt);
        validateAudience(jwt);
        validateVerifiedEmail(jwt);

        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account subject is required");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account email is required");
        }

        String fullName = jwt.getClaimAsString("name");
        if (fullName == null || fullName.isBlank()) {
            fullName = email.substring(0, email.indexOf('@'));
        }

        return new GoogleUserInfo(subject.trim(), email.trim().toLowerCase(), fullName.trim());
    }

    private void validateIssuer(Jwt jwt) {
        String issuer = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
        if (!"https://accounts.google.com".equals(issuer) && !"accounts.google.com".equals(issuer)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token issuer");
        }
    }

    private void validateAudience(Jwt jwt) {
        if (!jwt.getAudience().contains(clientId.trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token audience");
        }
    }

    private void validateVerifiedEmail(Jwt jwt) {
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
        }
    }
}
