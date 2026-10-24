package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.LoginRequest;
import gestion.reparabilidad.colision.dto.LoginResponse;
import gestion.reparabilidad.colision.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(response);
    }
}
