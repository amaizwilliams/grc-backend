package gestion.reparabilidad.colision.repository;


import gestion.reparabilidad.colision.modelo.OrdenEtapaFecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrdenEtapaFechaRepository extends JpaRepository<OrdenEtapaFecha, Long> {
    Optional<OrdenEtapaFecha> findByOrdenReparacionIdOrdenReparacionAndFechaFinIsNull(Long idOrden);

    boolean existsByOrdenReparacionIdOrdenReparacionAndEtapaIdEtapa(Long idOrden, Long idEtapa);

    long countByTecnicoIdPersonaAndFechaFinIsNull(Long idTecnico);

}
