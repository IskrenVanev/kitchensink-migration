package com.iskren.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.iskren.dto.AuthResponseDTO;
import com.iskren.dto.LoginRequestDTO;
import com.iskren.dto.RegisterRequestDTO;
import com.iskren.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthResourceRESTController {
    private final AuthService authService;

    public AuthResourceRESTController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        String token = authService.login(request);

        return ResponseEntity.ok(
                new AuthResponseDTO(token)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequestDTO request) {

        authService.register(request);

        return ResponseEntity.ok("User registered");
    }
}
