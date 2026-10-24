package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.usuario.UsuarioRequestDto;
import gestion.reparabilidad.colision.dto.usuario.UsuarioResponseDto;
import gestion.reparabilidad.colision.dto.usuario.UsuarioUpdateDto;
import gestion.reparabilidad.colision.exception.EmailAlreadyExistsException;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.modelo.Usuario;
import gestion.reparabilidad.colision.repository.UsuarioRepository;
import gestion.reparabilidad.colision.util.EmailUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<UsuarioResponseDto> getAllUsuario() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertirAEntityADto)
                .collect(Collectors.toList());
    }

    public List<UsuarioResponseDto> getAllUsuarioDesactivado() {
        return usuarioRepository.findByActivoFalse().stream()
                .map(this::convertirAEntityADto)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDto getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        return convertirAEntityADto(usuario);
    }

    public UsuarioResponseDto saveUsuario(UsuarioRequestDto requestDto) {
        String emailNormalizado = EmailUtil.normalizar(requestDto.email());

        // Validación: El email debe ser único
        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new EmailAlreadyExistsException("El email ya se encuentra registrado");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(requestDto.nombre());
        nuevoUsuario.setEmail(emailNormalizado);
        nuevoUsuario.setRol(requestDto.rol());
        nuevoUsuario.setActivo(true);

        String passwordEncriptada = passwordEncoder.encode(requestDto.password());
        nuevoUsuario.setPasswordHash(passwordEncriptada);

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        return convertirAEntityADto(usuarioGuardado);
    }

    public UsuarioResponseDto updateUsuario(Long id, UsuarioUpdateDto requestDto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        String emailNormalizado = EmailUtil.normalizar(requestDto.email());

        // Validar que el nuevo email no pertenezca a otro usuario
        usuarioRepository.findByEmail(emailNormalizado)
                .ifPresent(u -> {
                    if (!u.getIdUsuario().equals(id)) {
                        throw new EmailAlreadyExistsException("El email ya se encuentra registrado");
                    }
                });
        usuarioExistente.setEmail(emailNormalizado);
        usuarioExistente.setNombre(requestDto.nombre());
        usuarioExistente.setRol(requestDto.rol());

        if (requestDto.password() != null && !requestDto.password().isBlank()) {
            String passwordEncriptada = passwordEncoder.encode(requestDto.password());
            usuarioExistente.setPasswordHash(passwordEncriptada);
        }
        
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return convertirAEntityADto(usuarioActualizado);
    }

    public void deleteUsuarioLogically(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false); // No borramos de la BD, solo inactivamos
        usuarioRepository.save(usuario);
    }

    public UsuarioResponseDto reactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(true);
        return convertirAEntityADto(usuarioRepository.save(usuario));
    }


    // METODO AUXILIAR: Mapeo de Entidad a DTO Completo
    private UsuarioResponseDto convertirAEntityADto(Usuario usuario) {

        return new UsuarioResponseDto(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }
}
