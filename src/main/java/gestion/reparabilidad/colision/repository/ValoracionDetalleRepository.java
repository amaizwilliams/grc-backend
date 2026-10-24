package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.Valoracion;
import gestion.reparabilidad.colision.modelo.ValoracionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValoracionDetalleRepository extends JpaRepository<ValoracionDetalle, Long> {
    void deleteByValoracionIdValoracion(Long idValoracion);
}
