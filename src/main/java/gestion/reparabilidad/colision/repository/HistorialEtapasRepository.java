package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.HistorialEtapas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialEtapasRepository extends JpaRepository<HistorialEtapas, Long> {
}
