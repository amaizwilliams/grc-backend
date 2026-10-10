package gestion.reparabilidad.colision.repository;
import gestion.reparabilidad.colision.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    List<Cliente> findByActivoTrue();
    List<Cliente> findByActivoFalse();
    Optional<Cliente> findByDocumento(String documento);
}
