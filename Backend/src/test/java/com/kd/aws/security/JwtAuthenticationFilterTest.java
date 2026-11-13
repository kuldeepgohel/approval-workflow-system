package com.kd.aws.security;

import com.kd.aws.service.CustomUserDetailsService;
import com.kd.aws.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsMissing() throws Exception {

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("test@example.com");

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("valid-token", userDetails))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("test@example.com", authentication.getName());

        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).isTokenValid("valid-token", userDetails);
    }

    @Test
    void shouldNotAuthenticateUserWhenTokenIsInvalid() throws Exception {

        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("test@example.com");

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("invalid-token", userDetails))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldClearSecurityContextWhenTokenParsingFails() throws Exception {

        request.addHeader("Authorization", "Bearer malformed-token");

        when(jwtService.extractUsername("malformed-token"))
                .thenThrow(new RuntimeException("Invalid JWT"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldClearSecurityContextWhenUserIsNotFound() throws Exception {

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("unknown@example.com");

        when(userDetailsService.loadUserByUsername("unknown@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotLoadUserWhenAuthenticationAlreadyExists() throws Exception {

        // Arrange: Set an existing authentication
        var existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "existing@example.com",
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        request.addHeader("Authorization", "Bearer valid-token");

        // Act: Execute the JWT filter
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: Existing authentication should remain unchanged
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        assertEquals(
                "existing@example.com",
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        // JWT username extraction happens before the authentication check
        verify(jwtService).extractUsername("valid-token");

        // User details should not be loaded again
        verifyNoInteractions(userDetailsService);
    }
}