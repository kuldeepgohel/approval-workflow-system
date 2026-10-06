package com.kd.aws.service;

import com.kd.aws.dto.login.LoginRequestDTO;
import com.kd.aws.dto.login.LoginResponseDTO;
import com.kd.aws.entity.User;
import com.kd.aws.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication  = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->
                        new RuntimeException("User not found.")
                );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        assert userDetails != null;
        String token = jwtService.generateToken(userDetails);

        return new LoginResponseDTO(
                token,
                user.getEmail(),
                user.getRole().getName()
        );
    }
}
