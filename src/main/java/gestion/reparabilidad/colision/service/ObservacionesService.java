package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.observacion.ObservacionRequestDto;
import gestion.reparabilidad.colision.dto.observacion.ObservacionResponseDto;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.Enums.Ubicacion;
import gestion.reparabilidad.colision.modelo.Etapas;
import gestion.reparabilidad.colision.modelo.Observacion;
import gestion.reparabilidad.colision.modelo.OrdenReparacion;
import gestion.reparabilidad.colision.repository.ObservacionRepository;
import gestion.reparabilidad.colision.repository.OrdenEtapaFechaRepository;
import gestion.reparabilidad.colision.repository.OrdenReparacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ObservacionesService {
    private final ObservacionRepository observacionRepository;
    private final OrdenEtapaFechaRepository ordenEtapaFechaRepository;
    private final OrdenReparacionRepository ordenReparacionRepository;
    private final OrdenReparacionService ordenReparacionService;
    private final EtapasService etapasService;

    public ObservacionesService(ObservacionRepository observacionRepository, OrdenEtapaFechaRepository ordenEtapaFechaRepository, OrdenReparacionRepository ordenReparacionRepository, OrdenReparacionService ordenReparacionService, EtapasService etapasService) {
        this.observacionRepository = observacionRepository;
        this.ordenEtapaFechaRepository = ordenEtapaFechaRepository;
        this.ordenReparacionRepository = ordenReparacionRepository;
        this.ordenReparacionService = ordenReparacionService;
        this.etapasService = etapasService;
    }

    @Transactional
    public ObservacionResponseDto crearObservacion(Long idOrden, ObservacionRequestDto request) {
        OrdenReparacion orden = ordenReparacionService.buscarOrden(idOrden);

        if (orden.getEstado() == Estado.CANCELADA) {
            throw new ReglaNegocioException("La orden " + idOrden + " está CANCELADA; no admite observaciones");
        }

        // Sin etapa = observación de "Entrega", que solo existe en órdenes ya entregadas
        Etapas etapa = null;
        if (request.idEtapa() == null) {
            if (orden.getEstado() != Estado.CERRADA) {
                throw new ReglaNegocioException("La opción Entrega solo aplica a órdenes entregadas; la orden " + idOrden + " está " + orden.getEstado());
            }
        } else {
            etapa = etapasService.buscarEtapa(request.idEtapa());
            if (!ordenEtapaFechaRepository.existsByOrdenReparacionIdOrdenReparacionAndEtapaIdEtapa(idOrden, etapa.getIdEtapa())) {
                throw new ReglaNegocioException("La orden " + idOrden + " no ha pasado por la etapa " + etapa.getNombre());
            }
        }

        if (request.ubicacion() != null && orden.getEstado() == Estado.CERRADA) {
            throw new ReglaNegocioException("La orden " + idOrden + " ya fue entregada; su ubicación no se puede cambiar");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Ubicacion ubicacion = request.ubicacion() != null ? request.ubicacion() : orden.getUbicacionActual();
        if (ubicacion == null) {
            ubicacion = Ubicacion.EN_TALLER;
        }

        Observacion observacion = new Observacion();
        observacion.setOrdenReparacion(orden);
        observacion.setUsuario(ordenReparacionService.buscarUsuarioAutenticado());
        observacion.setEtapa(etapa);
        observacion.setUbicacion(ubicacion);
        observacion.setEstadoRepuestos(request.estadoRepuestos());
        observacion.setNota(request.nota().trim());
        observacion.setVisibleCliente(Boolean.TRUE.equals(request.visibleCliente()));
        observacion.setCreateAt(ahora);
        observacionRepository.save(observacion);

        // La observación es la que mueve la ubicación de la orden (p. ej. sale a esperar repuestos)
        if (request.ubicacion() != null && request.ubicacion() != orden.getUbicacionActual()) {
            orden.setUbicacionActual(request.ubicacion());
            orden.setUpdateAt(ahora);
            ordenReparacionRepository.save(orden);
        }

        return convertirADto(observacion);
    }

    @Transactional(readOnly = true)
    public List<ObservacionResponseDto> listarObservaciones(Long idOrden) {
        ordenReparacionService.buscarOrden(idOrden);
        return observacionRepository.findByOrdenReparacionIdOrdenReparacionOrderByCreateAtDesc(idOrden)
                .stream().map(this::convertirADto).toList();
    }

    private ObservacionResponseDto convertirADto(Observacion observacion) {
        Etapas etapa = observacion.getEtapa();
        return new ObservacionResponseDto(
                observacion.getIdObservacion(),
                observacion.getOrdenReparacion().getIdOrdenReparacion(),
                etapa != null ? etapa.getIdEtapa() : null,
                etapa != null ? etapa.getNombre() : "Entrega",
                observacion.getUsuario().getNombre(),
                observacion.getNota(),
                observacion.getUbicacion(),
                observacion.getEstadoRepuestos(),
                observacion.getVisibleCliente(),
                observacion.getImagenes().size(),
                observacion.getCreateAt()
        );
    }
}
