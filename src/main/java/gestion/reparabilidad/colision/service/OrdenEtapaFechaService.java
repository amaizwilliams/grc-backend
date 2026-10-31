package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.ordenEtapaFecha.OrdenEtapaFechaResponseDto;
import gestion.reparabilidad.colision.modelo.OrdenEtapaFecha;
import gestion.reparabilidad.colision.modelo.Tecnico;
import gestion.reparabilidad.colision.repository.OrdenEtapaFechaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenEtapaFechaService {

    private final OrdenReparacionService ordenReparacionService;
    private final OrdenEtapaFechaRepository ordenEtapaFechaRepository;

    public OrdenEtapaFechaService(OrdenReparacionService ordenReparacionService, OrdenEtapaFechaRepository ordenEtapaFechaRepository) {
        this.ordenReparacionService = ordenReparacionService;
        this.ordenEtapaFechaRepository = ordenEtapaFechaRepository;
    }

    @Transactional(readOnly = true)
    public List<OrdenEtapaFechaResponseDto> listarEtapasOrden(Long idOrden) {
        ordenReparacionService.buscarOrden(idOrden);
        return ordenEtapaFechaRepository.findByOrdenReparacionIdOrdenReparacionOrderByFechaInicioAsc(idOrden)
                .stream().map(this::convertirADto).toList();
    }

    private OrdenEtapaFechaResponseDto convertirADto(OrdenEtapaFecha etapaOrden) {
        Tecnico tecnico = etapaOrden.getTecnico();
        return new OrdenEtapaFechaResponseDto(
                etapaOrden.getIdOrdenEtapaFecha(),
                etapaOrden.getOrdenReparacion().getIdOrdenReparacion(),
                etapaOrden.getEtapa().getIdEtapa(),
                etapaOrden.getEtapa().getNombre(),
                tecnico == null ? null : tecnico.getIdPersona(),
                tecnico == null ? null : tecnico.getNombreCompleto(),
                etapaOrden.getFechaInicio(),
                etapaOrden.getFechaFin(),
                etapaOrden.getCompletada()
        );
    }
}
