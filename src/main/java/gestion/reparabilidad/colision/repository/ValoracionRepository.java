package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    Optional<Valoracion> findByOrdenReparacionIdOrdenReparacion(Long idOrden);
    boolean existsByOrdenReparacionIdOrdenReparacion(Long idOrden);
    List<Valoracion> findByCargadaCesvi(Boolean cargadaCesvi);
}
