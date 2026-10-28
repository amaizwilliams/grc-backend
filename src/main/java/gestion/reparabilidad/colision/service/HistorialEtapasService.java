package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.historialEtapas.HistorialEtapasResponseDto;
import gestion.reparabilidad.colision.modelo.HistorialEtapas;
import gestion.reparabilidad.colision.repository.HistorialEtapasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistorialEtapasService {

    private final OrdenReparacionService ordenReparacionService;
    private final HistorialEtapasRepository historialEtapasRepository;

    public HistorialEtapasService(OrdenReparacionService ordenReparacionService, HistorialEtapasRepository historialEtapasRepository) {
        this.ordenReparacionService = ordenReparacionService;
        this.historialEtapasRepository = historialEtapasRepository;
    }

    @Transactional(readOnly = true)
    public List<HistorialEtapasResponseDto> listarHistorialEtapas(Long idOrden){
        ordenReparacionService.buscarOrden(idOrden);
        return historialEtapasRepository.findByOrdenReparacionIdOrdenReparacionOrderByFechaCambioAsc(idOrden)
                .stream().map(this::convertirADto).toList();
    }

    private HistorialEtapasResponseDto convertirADto(HistorialEtapas historial){
        return new HistorialEtapasResponseDto(
                historial.getIdHistorialEtapas(),
                historial.getOrdenReparacion().getIdOrdenReparacion(),
                historial.getEtapa().getIdEtapa(),
                historial.getEtapa().getNombre(),
                historial.getUsuario().getNombre(),
                historial.getFechaCambio()
        );
    }
}
