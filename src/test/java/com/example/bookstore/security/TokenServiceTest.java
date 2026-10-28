package com.example.bookstore.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenServiceTest {
    @Test
    void createsBearerTokenThroughConfiguredJwtEncoder() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        TokenService service = new TokenService(encoder);
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(
            new Jwt("signed.jwt.token", Instant.now(), Instant.now().plusSeconds(3600),
                java.util.Map.of("alg", "RS256"), java.util.Map.of("sub", "user@example.com")));

        String token = service.create("user@example.com", "USER");

        assertThat(token).isEqualTo("signed.jwt.token");
        verify(encoder).encode(any(JwtEncoderParameters.class));
    }
}
