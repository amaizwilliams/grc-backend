package gestion.reparabilidad.colision.repository;


import gestion.reparabilidad.colision.modelo.Etapas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapasRepository extends JpaRepository<Etapas, Long> {

    List<Etapas> findAllByOrderByOrdenAsc();

    boolean existsByOrden(Integer orden);
    boolean existsByOrdenAndIdEtapaNot(Integer orden, Long idEtapa);

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdEtapaNot(String nombre, Long idEtapa);
}
