package gestion.reparabilidad.colision.repository;

import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.OrdenReparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenReparacionRepository extends JpaRepository<OrdenReparacion, Long> {

    List<OrdenReparacion> findByEstado(Estado estado);                    // listado de activas
    List<OrdenReparacion> findByVehiculoPlaca(String placa);
    List<OrdenReparacion> findByEstadoOrderByFechaDeEntregaRealDesc(Estado estado);
    List<OrdenReparacion> findByEstadoAndValoracionIsNull(Estado estado);

}
