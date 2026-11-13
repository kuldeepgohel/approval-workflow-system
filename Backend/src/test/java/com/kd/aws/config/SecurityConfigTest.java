
package com.kd.aws.config;

import com.kd.aws.security.JwtAuthenticationFilter;
import com.kd.aws.service.CustomUserDetailsService;
import com.kd.aws.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(classes = SecurityConfigTest.TestConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(get("/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(
                        "Please login with valid credentials."));
    }

    @Test
    void shouldAllowAccessToPublicAuthEndpoint() throws Exception {

        mockMvc.perform(get("/api/auth/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("Public endpoint"));
    }

    @Test
    void shouldAllowAuthenticatedUserWithRequiredRole() throws Exception {

        mockMvc.perform(get("/test/user")
                        .with(user(
                                User.withUsername("user@example.com")
                                        .password("password")
                                        .roles("USER")
                                        .build()
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string("User endpoint"));
    }

    @Test
    void shouldReturn403WhenUserDoesNotHaveRequiredRole() throws Exception {

        mockMvc.perform(get("/test/admin")
                        .with(user(
                                User.withUsername("user@example.com")
                                        .password("password")
                                        .roles("USER")
                                        .build()
                        )))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @TestConfiguration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    @Import({
            SecurityConfig.class,
            TestProtectedController.class,
            TestPublicController.class
    })
    static class TestConfig {

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(
                JwtService jwtService,
                CustomUserDetailsService customUserDetailsService) {

            return new JwtAuthenticationFilter(
                    jwtService,
                    customUserDetailsService
            );
        }
    }

    @RestController
    static class TestProtectedController {

        @GetMapping("/test/protected")
        public String protectedEndpoint() {
            return "Protected endpoint";
        }

        @GetMapping("/test/user")
        @org.springframework.security.access.prepost.PreAuthorize(
                "hasRole('USER')")
        public String userEndpoint() {
            return "User endpoint";
        }

        @GetMapping("/test/admin")
        @org.springframework.security.access.prepost.PreAuthorize(
                "hasRole('ADMIN')")
        public String adminEndpoint() {
            return "Admin endpoint";
        }
    }

    @RestController
    static class TestPublicController {

        @GetMapping("/api/auth/public")
        public String publicEndpoint() {
            return "Public endpoint";
        }
    }
}
