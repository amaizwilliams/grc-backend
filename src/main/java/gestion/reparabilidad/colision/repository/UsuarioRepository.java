package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoFalse();
}
