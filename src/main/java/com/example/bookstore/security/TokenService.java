package com.example.bookstore.security;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private final JwtEncoder encoder;

    public TokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String create(String email, String role) {
        log.debug("Creating JWT for authenticated principal with role={}", role);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("bookstore-api").subject(email).issuedAt(now).expiresAt(now.plusSeconds(3600)).claim("scope", role).build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        log.debug("JWT created successfully");
        return token;
    }
}
