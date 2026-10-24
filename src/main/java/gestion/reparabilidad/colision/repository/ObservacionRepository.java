package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    List<Observacion> findByOrdenReparacionIdOrdenReparacionOrderByCreateAtDesc(Long idOrden);

}
