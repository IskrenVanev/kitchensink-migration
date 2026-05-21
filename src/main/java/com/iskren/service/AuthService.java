package com.iskren.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iskren.dto.LoginRequestDTO;
import com.iskren.dto.RegisterRequestDTO;
import com.iskren.model.Role;
import com.iskren.model.User;
import com.iskren.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;   
    }

    public String login(LoginRequestDTO request) {

        User user = userRepository.findByUsername(
                request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        user.getUsername());

        return jwtService.generateToken(userDetails);
    }

    public void register(RegisterRequestDTO request){

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);

        user.setMemberId(request.getMemberId());

        userRepository.save(user);
    }
}
