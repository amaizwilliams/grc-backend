package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.HistorialEtapas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialEtapasRepository extends JpaRepository<HistorialEtapas, Long> {
    List<HistorialEtapas> findByOrdenReparacionIdOrdenReparacionOrderByFechaCambioAsc(Long idOrden);
    Optional<HistorialEtapas> findFirstByOrdenReparacionIdOrdenReparacionOrderByFechaCambioDesc(Long idOrden);
}
