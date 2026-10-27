package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.ordenReparacion.OrdenReparacionResponseDto;
import gestion.reparabilidad.colision.dto.valoracion.*;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Enums.Accion;
import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.Enums.FiltroValoracion;
import gestion.reparabilidad.colision.modelo.OrdenReparacion;
import gestion.reparabilidad.colision.modelo.Valoracion;
import gestion.reparabilidad.colision.modelo.ValoracionDetalle;
import gestion.reparabilidad.colision.repository.OrdenReparacionRepository;
import gestion.reparabilidad.colision.repository.ValoracionDetalleRepository;
import gestion.reparabilidad.colision.repository.ValoracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final ValoracionDetalleRepository valoracionDetalleRepository;
    private final OrdenReparacionService ordenReparacionService;
    private final OrdenReparacionRepository ordenReparacionRepository;

    public ValoracionService(ValoracionRepository valoracionRepository, ValoracionDetalleRepository valoracionDetalleRepository, OrdenReparacionService ordenReparacionService, OrdenReparacionRepository ordenReparacionRepository) {
        this.valoracionRepository = valoracionRepository;
        this.valoracionDetalleRepository = valoracionDetalleRepository;
        this.ordenReparacionService = ordenReparacionService;
        this.ordenReparacionRepository = ordenReparacionRepository;
    }

    private void validarDetalles(List<ValoracionDetalleRequestDto> detalles) {
        for (ValoracionDetalleRequestDto detalle : detalles) {
            if (detalle.accion() == Accion.REPARACION && detalle.gravedad() == null) {
                throw new ReglaNegocioException("La pieza " + detalle.pieza() + " es una reparación: indique la gravedad");
            }
            if (detalle.accion() == Accion.SUSTITUCION && detalle.gravedad() != null) {
                throw new ReglaNegocioException("La pieza " + detalle.pieza() + " es una sustitución: no lleva gravedad");
            }
        }
    }

    private List<ValoracionDetalle> guardarDetalles(Valoracion valoracion, List<ValoracionDetalleRequestDto> detallesDto) {
        List<ValoracionDetalle> detalles = new ArrayList<>();
        for (ValoracionDetalleRequestDto dto : detallesDto) {
            ValoracionDetalle detalle = new ValoracionDetalle();
            detalle.setValoracion(valoracion);
            detalle.setPieza(dto.pieza().trim());
            detalle.setAccion(dto.accion());
            detalle.setGravedad(dto.gravedad());
            detalle.setCantidad(dto.cantidad() != null ? dto.cantidad().shortValue() : 1);
            detalle.setObservacion(limpiar(dto.observacion()));
            detalles.add(detalle);
        }
        return valoracionDetalleRepository.saveAll(detalles);
    }

    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    @Transactional
    public ValoracionResponseDto crearValoracion(Long idOrden, ValoracionRequestDto request) {
        OrdenReparacion orden = ordenReparacionService.buscarOrden(idOrden);
        ordenReparacionService.validarActiva(orden);
        if (valoracionRepository.existsByOrdenReparacionIdOrdenReparacion(idOrden)) {
            throw new ReglaNegocioException("La orden " + idOrden + " ya tiene valoración; edítela en lugar de crear otra");
        }
        validarDetalles(request.detalles());
        LocalDateTime ahora = LocalDateTime.now();
        Valoracion valoracion = new Valoracion();
        valoracion.setOrdenReparacion(orden);
        valoracion.setUsuario(ordenReparacionService.buscarUsuarioAutenticado());
        valoracion.setDescripcionGeneral(limpiar(request.descripcionGeneral()));
        valoracion.setCargadaCesvi(false);
        valoracion.setCreateAt(ahora);
        valoracionRepository.save(valoracion);
        List<ValoracionDetalle> detalles = guardarDetalles(valoracion, request.detalles());
        if (orden.getFechaIngresoCotizar() == null) {
            ordenReparacionService.aplicarIngresoCotizar(orden, ahora.toLocalDate());
            ordenReparacionRepository.save(orden);
        }

        return convertirADto(valoracion, detalles);

    }

    @Transactional
    public ValoracionResponseDto editarValoracion(Long idOrden, ValoracionRequestDto request) {
        OrdenReparacion orden = ordenReparacionService.buscarOrden(idOrden);
        ordenReparacionService.validarActiva(orden);
        Valoracion valoracion = valoracionRepository.findByOrdenReparacionIdOrdenReparacion(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("La orden " + idOrden + " no tiene valoración"));
        validarDetalles(request.detalles());
        valoracion.setDescripcionGeneral(limpiar(request.descripcionGeneral()));
        valoracion.setUpdateAt(LocalDateTime.now());
        valoracionRepository.save(valoracion);
        valoracionDetalleRepository.deleteByValoracionIdValoracion(valoracion.getIdValoracion());
        List<ValoracionDetalle> detalles = guardarDetalles(valoracion, request.detalles());
        return convertirADto(valoracion, detalles);
    }

    private ValoracionResponseDto convertirADto(Valoracion valoracion, List<ValoracionDetalle> detalles) {
        // SUSTITUCION arriba, REPARACION abajo; dentro de cada grupo, alfabético por pieza
        List<ValoracionDetalleResponseDto> detallesDto = detalles.stream()
                .sorted(Comparator.comparing((ValoracionDetalle d) -> d.getAccion() != Accion.SUSTITUCION)
                        .thenComparing(d -> d.getPieza().toLowerCase()))
                .map(this::convertirDetalleADto)
                .toList();

        return new ValoracionResponseDto(
                valoracion.getIdValoracion(),
                valoracion.getOrdenReparacion().getIdOrdenReparacion(),
                valoracion.getOrdenReparacion().getVehiculo().getPlaca(),
                valoracion.getDescripcionGeneral(),
                valoracion.getUsuario().getNombre(),
                valoracion.getCargadaCesvi(),
                valoracion.getCargadaCesviAt(),
                valoracion.getCreateAt(),
                valoracion.getUpdateAt(),
                detallesDto,
                valoracion.getImagenes().size()
        );
    }

    private ValoracionDetalleResponseDto convertirDetalleADto(ValoracionDetalle detalle) {
        return new ValoracionDetalleResponseDto(
                detalle.getIdValoracionDetalle(),
                detalle.getPieza(),
                detalle.getAccion(),
                detalle.getGravedad(),
                detalle.getCantidad(),
                detalle.getObservacion()
        );
    }

    @Transactional(readOnly = true)
    public ValoracionResponseDto obtenerValoracion(Long idOrden) {
        ordenReparacionService.buscarOrden(idOrden);   // 404 si la orden no existe

        Valoracion valoracion = valoracionRepository.findByOrdenReparacionIdOrdenReparacion(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("La orden " + idOrden + " no tiene valoración"));

        return convertirADto(valoracion, valoracion.getDetalles());
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponseDto> listarOrdenesPorFiltro(FiltroValoracion filtro) {
        List<OrdenReparacion> ordenes = switch (filtro) {
            case SIN_VALORAR -> ordenReparacionRepository.findByEstadoAndValoracionIsNull(Estado.ACTIVA);
            case SIN_CARGAR ->
                    ordenReparacionRepository.findByValoracionCargadaCesviAndEstadoNot(false, Estado.CANCELADA);
            case CARGADAS -> ordenReparacionRepository.findByValoracionCargadaCesviAndEstadoNot(true, Estado.CANCELADA);
        };
        return ordenes.stream()
                .map(ordenReparacionService::convertirADto)
                .toList();
    }

    @Transactional
    public ValoracionResponseDto marcarCargadaCesvi(Long idOrden) {
        OrdenReparacion orden = ordenReparacionService.buscarOrden(idOrden);
        if (orden.getEstado() == Estado.CANCELADA) {
            throw new ReglaNegocioException("La orden " + idOrden + " está cancelada");
        }
        Valoracion valoracion = valoracionRepository.findByOrdenReparacionIdOrdenReparacion(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("La orden " + idOrden + " no tiene valoración"));
        if (Boolean.TRUE.equals(valoracion.getCargadaCesvi())) {
            throw new ReglaNegocioException("La valoración de la orden " + idOrden + " ya está cargada a CESVI");
        }
        LocalDateTime ahora = LocalDateTime.now();
        valoracion.setCargadaCesvi(true);
        valoracion.setCargadaCesviAt(ahora);
        valoracion.setUpdateAt(ahora);
        valoracionRepository.save(valoracion);
        return convertirADto(valoracion, valoracion.getDetalles());
    }

    @Transactional(readOnly = true)
    public ConteoValoracionResponseDto contarPorFiltro() {
        long sinValorar = ordenReparacionRepository.countByEstadoAndValoracionIsNull(Estado.ACTIVA);
        long sinCargar = ordenReparacionRepository.countByValoracionCargadaCesviAndEstadoNot(false, Estado.CANCELADA);
        long cargadas = ordenReparacionRepository.countByValoracionCargadaCesviAndEstadoNot(true, Estado.CANCELADA);
        return new ConteoValoracionResponseDto(sinValorar, sinCargar, cargadas);

    }

}
