
package com.kd.aws.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private UserDetails userDetails;

    private final String secret =
            "my-test-secret-key-for-jwt-testing-12345678901234567890";

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        // Inject the JWT secret into the @Value field
        ReflectionTestUtils.setField(jwtService, "secret", secret);

        signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "test@example.com",
                jwtService.extractUsername(token)
        );
    }

    @Test
    void shouldValidateTokenSuccessfully() {

        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenTokenUsernameDoesNotMatch() {

        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = User.builder()
                .username("another@example.com")
                .password("password")
                .roles("USER")
                .build();

        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertFalse(isValid);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {

        String expiredToken = createExpiredToken();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(expiredToken, userDetails)
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsMalformed() {

        String malformedToken = "invalid.jwt.token";

        assertThrows(
                MalformedJwtException.class,
                () -> jwtService.extractUsername(malformedToken)
        );
    }

    private String createExpiredToken() {

        Date currentDate = new Date();

        Date issuedAt = new Date(
                currentDate.getTime() - (2 * 60 * 1000)
        );

        Date expirationDate = new Date(
                currentDate.getTime() - (60 * 1000)
        );

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(signingKey)
                .compact();
    }
}
