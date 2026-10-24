package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.ordenReparacion.*;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.*;
import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.Enums.Ubicacion;
import gestion.reparabilidad.colision.repository.*;
import gestion.reparabilidad.colision.util.PlacaUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdenReparacionService {
    //dependencias//
    private final OrdenReparacionRepository ordenReparacionRepository;
    private final VehiculoService vehiculoService;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final OrdenEtapaFechaRepository ordenEtapaFechaRepository;
    private final HistorialEtapasRepository historialEtapasRepository;
    private final ObservacionRepository observacionRepository;
    private final EtapasService etapasService;
    private final TecnicoService tecnicoService;

    public OrdenReparacionService(OrdenReparacionRepository ordenReparacionRepository, VehiculoService vehiculoService, ClienteService clienteService, UsuarioRepository usuarioRepository, OrdenEtapaFechaRepository ordenEtapaFechaRepository, HistorialEtapasRepository historialEtapasRepository, ObservacionRepository observacionRepository, EtapasService etapasService, TecnicoService tecnicoService) {
        this.ordenReparacionRepository = ordenReparacionRepository;
        this.vehiculoService = vehiculoService;
        this.clienteService = clienteService;
        this.usuarioRepository = usuarioRepository;
        this.ordenEtapaFechaRepository = ordenEtapaFechaRepository;
        this.historialEtapasRepository = historialEtapasRepository;
        this.observacionRepository = observacionRepository;
        this.etapasService = etapasService;
        this.tecnicoService = tecnicoService;
    }

    @Transactional
    public OrdenReparacionResponseDto saveOrden(OrdenReparacionRequestDto request) {
        Vehiculo vehiculo = vehiculoService.buscarVehiculo(request.idVehiculo());
        Cliente cliente = clienteService.buscarCliente(request.idCliente());
        Usuario creador = buscarUsuarioAutenticado();

        OrdenReparacion orden = new OrdenReparacion();
        orden.setVehiculo(vehiculo);
        orden.setCliente(cliente);
        orden.setUsuarioCreador(creador);
        orden.setEstado(Estado.ACTIVA);
        orden.setCreateAt(LocalDateTime.now());
        return convertirADto(ordenReparacionRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public OrdenReparacionResponseDto getOrdenById(Long id) {
        return convertirADto(buscarOrden(id));
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponseDto> getAllOrdenActivas() {
        return ordenReparacionRepository.findByEstado(Estado.ACTIVA).stream().map(this::convertirADto).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponseDto> getOrdenesByPlaca(String placa) {
        String placaNormalizada = PlacaUtil.normalizar(placa);
        return ordenReparacionRepository.findByVehiculoPlaca(placaNormalizada).stream().map(this::convertirADto).toList();
    }

    @Transactional
    public OrdenReparacionResponseDto updateOrden(Long id, OrdenReparacionRequestDto datos) {
        OrdenReparacion orden = buscarOrden(id);
        if (orden.getEstado() != Estado.ACTIVA) {
            throw new ReglaNegocioException("Solo se puede editar una orden activa; la orden " + id + " está " + orden.getEstado());
        }
        orden.setVehiculo(vehiculoService.buscarVehiculo(datos.idVehiculo()));
        orden.setCliente(clienteService.buscarCliente(datos.idCliente()));
        orden.setUpdateAt(LocalDateTime.now());
        return convertirADto(ordenReparacionRepository.save(orden));
    }

    @Transactional
    public void deleteOrdenById(Long id) {
        OrdenReparacion orden = buscarOrden(id);
        orden.setEstado(Estado.CANCELADA);
        orden.setUpdateAt(LocalDateTime.now());
        ordenReparacionRepository.save(orden);
    }

    @Transactional
    public OrdenReparacionResponseDto reactivarOrden(Long id) {
        OrdenReparacion orden = buscarOrden(id);
        if (orden.getEstado() != Estado.CANCELADA) {
            throw new ReglaNegocioException("Solo se puede reactivar una orden cancelada; la orden " + id + " está " + orden.getEstado());
        }
        orden.setEstado(Estado.ACTIVA);
        orden.setUpdateAt(LocalDateTime.now());
        return convertirADto(ordenReparacionRepository.save(orden));
    }

    public OrdenReparacion buscarOrden(Long id) {
        return ordenReparacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden de reparación con id " + id));
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponseDto> getOrdenesEntregadas() {
        return ordenReparacionRepository.findByEstadoOrderByFechaDeEntregaRealDesc(Estado.CERRADA)
                .stream().map(this::convertirADto).toList();
    }

    public void validarActiva(OrdenReparacion orden) {
        if (orden.getEstado() != Estado.ACTIVA) {
            throw new ReglaNegocioException("solo se puede modificar una orden activa");
        }
    }

    public void aplicarIngresoCotizar(OrdenReparacion orden, LocalDate fecha) {
        orden.setFechaIngresoCotizar(fecha);
        orden.setUpdateAt(LocalDateTime.now());
    }

    @Transactional
    public OrdenReparacionResponseDto marcarIngresoTaller(Long idOrden, FechaRequestDto request) {
        OrdenReparacion orden = buscarOrden(idOrden);
        validarActiva(orden);
        if (orden.getFechaIngresoReparacion() != null) {
            throw new ReglaNegocioException("La orden " + idOrden + " ya tiene la fecha de ingreso llena (" + orden.getFechaIngresoReparacion() + ")");
        }
        if (orden.getFechaIngresoCotizar() != null && request.fecha().isBefore(orden.getFechaIngresoCotizar())) {
            throw new ReglaNegocioException("La fecha de ingreso a reparacion no puede ser anterior a la de ingreso a cotizar");
        }
        orden.setUbicacionActual(Ubicacion.EN_TALLER);
        orden.setFechaIngresoReparacion(request.fecha());
        orden.setUpdateAt(LocalDateTime.now());
        return convertirADto(ordenReparacionRepository.save(orden));
    }

    @Transactional
    public OrdenReparacionResponseDto marcarIngresoCotizar(Long idOrden, FechaRequestDto request) {
        OrdenReparacion orden = buscarOrden(idOrden);
        validarActiva(orden);

        if (orden.getFechaIngresoCotizar() != null) {
            throw new ReglaNegocioException("La orden " + idOrden + " ya tiene fecha de ingreso a cotizar ("
                    + orden.getFechaIngresoCotizar() + ")");
        }
        if (orden.getFechaIngresoReparacion() != null && request.fecha().isAfter(orden.getFechaIngresoReparacion())) {
            throw new ReglaNegocioException("La fecha de ingreso a cotizar no puede ser posterior al ingreso al taller ("
                    + orden.getFechaIngresoReparacion() + ")");
        }

        aplicarIngresoCotizar(orden, request.fecha());
        return convertirADto(ordenReparacionRepository.save(orden));
    }

    @Transactional
    public OrdenReparacionResponseDto marcarDiasEstimados(Long idOrden, DiasEstimadosRequestDto request) {
        OrdenReparacion orden = buscarOrden(idOrden);
        validarActiva(orden);
        if (orden.getFechaIngresoReparacion() == null) {
            throw new ReglaNegocioException("No se puede marcar los dias estimados sin marcar la fecha de ingreso a reparacion, marcala antes de marcar los dias estimados");
        }
        if (orden.getDiasEstimadoEntrega() != null) {
            throw new ReglaNegocioException("La orden " + idOrden + " ya tiene días estimados de entrega ("
                    + orden.getDiasEstimadoEntrega() + ")");
        }

        orden.setDiasEstimadoEntrega(request.dias().byteValue());
        orden.setFechaDeEntregaEstimada(orden.getFechaIngresoReparacion().plusDays(request.dias()));
        orden.setUpdateAt(LocalDateTime.now());
        return convertirADto(ordenReparacionRepository.save(orden));

    }

    // El filtro JWT deja el email del usuario como nombre de la autenticación
    public Usuario buscarUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
    }

    @Transactional
    public OrdenReparacionResponseDto cambiarEtapa(Long idOrden, CambioEtapaRequestDto request) {
        OrdenReparacion orden = buscarOrden(idOrden);

        if (orden.getEstado() != Estado.ACTIVA) {
            throw new ReglaNegocioException("Solo se puede cambiar de etapa una orden activa; la orden " + idOrden + " está " + orden.getEstado());
        }

        if (orden.getFechaIngresoReparacion() == null) {
            throw new ReglaNegocioException("La orden " + idOrden + " no tiene fecha de ingreso a reparación; márquela antes de asignar una etapa");
        }

        Etapas destino = etapasService.buscarEtapa(request.idEtapaDestino());

        if (ordenEtapaFechaRepository.existsByOrdenReparacionIdOrdenReparacionAndEtapaIdEtapa(idOrden, destino.getIdEtapa())) {
            throw new ReglaNegocioException("La orden " + idOrden + " ya pasó por la etapa " + destino.getNombre());
        }
        Tecnico tecnico = null;
        if (destino.getEspecialidad() == null) {
            if (request.idTecnico() != null) {
                throw new ReglaNegocioException("La etapa " + destino.getNombre() + " no lleva técnico");
            }
        } else {
            if (request.idTecnico() == null) {
                throw new ReglaNegocioException("La etapa " + destino.getNombre() + " requiere un técnico");
            }
            tecnico = tecnicoService.buscarTecnico(request.idTecnico());
            if (!tecnico.getActivo()) {
                throw new ReglaNegocioException("El técnico " + tecnico.getNombreCompleto() + " está desactivado");
            }
            if (tecnico.getEspecialidad() != destino.getEspecialidad()) {
                throw new ReglaNegocioException("La etapa " + destino.getNombre() + " requiere un técnico "
                        + destino.getEspecialidad() + "; " + tecnico.getNombreCompleto() + " es " + tecnico.getEspecialidad());
            }
        }

        LocalDateTime ahora = LocalDateTime.now();
        Usuario usuario = buscarUsuarioAutenticado();

        ordenEtapaFechaRepository.findByOrdenReparacionIdOrdenReparacionAndFechaFinIsNull(idOrden)
                .ifPresent(abierta -> {
                    abierta.setFechaFin(ahora);
                    abierta.setCompletada(true);
                    ordenEtapaFechaRepository.save(abierta);
                });

        OrdenEtapaFecha nuevaEtapa = new OrdenEtapaFecha();
        nuevaEtapa.setOrdenReparacion(orden);
        nuevaEtapa.setEtapa(destino);
        nuevaEtapa.setTecnico(tecnico);
        nuevaEtapa.setFechaInicio(ahora);
        nuevaEtapa.setCompletada(false);
        ordenEtapaFechaRepository.save(nuevaEtapa);
        HistorialEtapas historial = new HistorialEtapas();
        historial.setOrdenReparacion(orden);
        historial.setEtapa(destino);
        historial.setUsuario(usuario);
        historial.setFechaCambio(ahora);
        historialEtapasRepository.save(historial);
        if (request.nota() != null && !request.nota().isBlank()) {
            Observacion observacion = new Observacion();
            observacion.setOrdenReparacion(orden);
            observacion.setUsuario(usuario);
            observacion.setEtapa(destino);
            observacion.setUbicacion(Ubicacion.EN_TALLER);
            observacion.setNota(request.nota().trim());
            observacion.setVisibleCliente(Boolean.TRUE.equals(request.visibleCliente()));
            observacion.setCreateAt(ahora);
            observacionRepository.save(observacion);
        }
        orden.setEtapaActual(destino);
        orden.setUpdateAt(ahora);
        return convertirADto(ordenReparacionRepository.save(orden));

    }

    @Transactional
    public OrdenReparacionResponseDto entregarOrden(Long idOrden, EntregaRequestDto request) {
        OrdenReparacion orden = buscarOrden(idOrden);
        validarActiva(orden);
        if (orden.getFechaIngresoReparacion() == null) {        // nunca entró al taller → 409
            throw new ReglaNegocioException("La orden " + idOrden + " no tiene fecha de ingreso a reparación; no se puede entregar");
        }

        LocalDateTime ahora = LocalDateTime.now();
        ordenEtapaFechaRepository.findByOrdenReparacionIdOrdenReparacionAndFechaFinIsNull(idOrden)
                .ifPresent(abierta -> {
                    abierta.setFechaFin(ahora);
                    abierta.setCompletada(true);
                    ordenEtapaFechaRepository.save(abierta);
                });
        if (request.nota() != null && !request.nota().isBlank()) {
            Observacion observacion = new Observacion();
            observacion.setOrdenReparacion(orden);
            observacion.setUsuario(buscarUsuarioAutenticado());
            observacion.setEtapa(null);                          // NULL = "Entrega"
            observacion.setUbicacion(Ubicacion.FUERA_DE_TALLER);
            observacion.setNota(request.nota().trim());
            observacion.setVisibleCliente(Boolean.TRUE.equals(request.visibleCliente()));
            observacion.setCreateAt(ahora);
            observacionRepository.save(observacion);
        }
        orden.setEstado(Estado.CERRADA);
        orden.setFechaDeEntregaReal(ahora.toLocalDate());
        orden.setUbicacionActual(Ubicacion.FUERA_DE_TALLER);
        orden.setUpdateAt(ahora);
        return convertirADto(ordenReparacionRepository.save(orden));

    }

    private OrdenReparacionResponseDto convertirADto(OrdenReparacion ordenReparacion) {
        Etapas etapa = ordenReparacion.getEtapaActual();
        return new OrdenReparacionResponseDto(
                ordenReparacion.getIdOrdenReparacion(),
                vehiculoService.convertirADto(ordenReparacion.getVehiculo()),
                clienteService.convertirADto(ordenReparacion.getCliente()),
                ordenReparacion.getUsuarioCreador().getNombre(),
                etapa == null ? null : etapa.getIdEtapa(),
                etapa == null ? null : etapa.getNombre(),
                ordenReparacion.getEstado(),
                ordenReparacion.getUbicacionActual(),
                ordenReparacion.getFechaIngresoCotizar(),
                ordenReparacion.getFechaIngresoReparacion(),
                ordenReparacion.getFechaDeEntregaEstimada(),
                ordenReparacion.getFechaDeEntregaReal(),
                ordenReparacion.getDiasEstimadoEntrega(),
                ordenReparacion.getCreateAt(),
                ordenReparacion.getUpdateAt()
        );
    }


}
