package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.ordenReparacion.OrdenReparacionRequestDto;
import gestion.reparabilidad.colision.dto.ordenReparacion.OrdenReparacionResponseDto;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Cliente;
import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.OrdenReparacion;
import gestion.reparabilidad.colision.modelo.Usuario;
import gestion.reparabilidad.colision.modelo.Vehiculo;
import gestion.reparabilidad.colision.repository.OrdenReparacionRepository;
import gestion.reparabilidad.colision.repository.UsuarioRepository;
import gestion.reparabilidad.colision.util.PlacaUtil;
import gestion.reparabilidad.colision.modelo.Etapas;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdenReparacionService {
    private final OrdenReparacionRepository ordenReparacionRepository;
    private final VehiculoService vehiculoService;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;

    public OrdenReparacionService(OrdenReparacionRepository ordenReparacionRepository,
                                  VehiculoService vehiculoService,
                                  ClienteService clienteService,
                                  UsuarioRepository usuarioRepository) {
        this.ordenReparacionRepository = ordenReparacionRepository;
        this.vehiculoService = vehiculoService;
        this.clienteService = clienteService;
        this.usuarioRepository = usuarioRepository;
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

    private OrdenReparacion buscarOrden(Long id) {
        return ordenReparacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden de reparación con id " + id));
    }

    // El filtro JWT deja el email del usuario como nombre de la autenticación
    private Usuario buscarUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
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
