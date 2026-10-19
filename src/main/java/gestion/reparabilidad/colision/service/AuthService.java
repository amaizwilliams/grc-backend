package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.exception.CredencialesInvalidasException;
import gestion.reparabilidad.colision.exception.UsuarioInactivoException;
import gestion.reparabilidad.colision.modelo.Usuario;
import gestion.reparabilidad.colision.repository.UsuarioRepository;
import gestion.reparabilidad.colision.util.EmailUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String passwordPlana) {
        Usuario usuario = usuarioRepository.findByEmail(EmailUtil.normalizar(email))
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsuarioInactivoException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(passwordPlana, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }

        return jwtService.generarToken(usuario);
    }
}
