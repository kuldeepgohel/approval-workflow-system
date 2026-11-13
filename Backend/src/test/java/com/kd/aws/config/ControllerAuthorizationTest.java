
package com.kd.aws.config;

import com.kd.aws.controller.RequestController;
import com.kd.aws.controller.UserController;
import com.kd.aws.security.JwtAuthenticationFilter;
import com.kd.aws.service.CustomUserDetailsService;
import com.kd.aws.service.JwtService;
import com.kd.aws.service.RequestService;
import com.kd.aws.service.UserService;

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
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(classes = ControllerAuthorizationTest.TestConfig.class)
class ControllerAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(
                        org.springframework.security.test.web.servlet.setup
                                .SecurityMockMvcConfigurers.springSecurity()
                )
                .build();
    }

    // USER should not create another user

    @Test
    void shouldReturn403WhenUserTriesToCreateUser() throws Exception {

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test User",
                                    "email": "test@example.com",
                                    "password": "password123",
                                    "departmentId": 1,
                                    "roleId": 1
                                }
                                """)
                        .with(user(
                                User.withUsername("user@example.com")
                                        .password("password")
                                        .roles("USER")
                                        .build()
                        )))
                .andExpect(status().isForbidden());
    }

    // ADMIN should create a user

    @Test
    void shouldAllowAdminToCreateUser() throws Exception {

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test User",
                                    "email": "test@example.com",
                                    "password": "password123",
                                    "departmentId": 1,
                                    "roleId": 1
                                }
                                """)
                        .with(user(
                                User.withUsername("admin@example.com")
                                        .password("password")
                                        .roles("ADMIN")
                                        .build()
                        )))
                .andExpect(status().isCreated());
    }

    // USER should not delete a request

    @Test
    void shouldReturn403WhenUserTriesToDeleteRequest() throws Exception {

        mockMvc.perform(delete("/api/requests/1")
                        .with(user(
                                User.withUsername("user@example.com")
                                        .password("password")
                                        .roles("USER")
                                        .build()
                        )))
                .andExpect(status().isForbidden());
    }

    // ADMIN should delete a request

    @Test
    void shouldAllowAdminToDeleteRequest() throws Exception {

        mockMvc.perform(delete("/api/requests/1")
                        .with(user(
                                User.withUsername("admin@example.com")
                                        .password("password")
                                        .roles("ADMIN")
                                        .build()
                        )))
                .andExpect(status().isNoContent());
    }

    @TestConfiguration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    @Import({
            SecurityConfig.class,
            UserController.class,
            RequestController.class
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
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        RequestService requestService() {
            return mock(RequestService.class);
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
}
