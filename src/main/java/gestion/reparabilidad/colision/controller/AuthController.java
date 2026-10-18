package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.LoginRequest;
import gestion.reparabilidad.colision.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
