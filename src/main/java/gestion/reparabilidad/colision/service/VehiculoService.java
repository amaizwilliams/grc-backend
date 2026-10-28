package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.vehiculo.VehiculoRequestDto;
import gestion.reparabilidad.colision.dto.vehiculo.VehiculoResponseDto;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.modelo.Cliente;
import gestion.reparabilidad.colision.modelo.Vehiculo;
import gestion.reparabilidad.colision.repository.VehiculoRepository;
import gestion.reparabilidad.colision.util.PlacaUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ClienteService clienteService;

    public VehiculoService(VehiculoRepository vehiculoRepository, ClienteService clienteService) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteService = clienteService;
    }

    public VehiculoResponseDto saveVehiculo(VehiculoRequestDto request){
        // 1. buscar el cliente dueño; 404 si no existe
        Cliente cliente = clienteService.buscarCliente(request.idCliente());
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setCliente(cliente);
        vehiculo.setPlaca(PlacaUtil.normalizar(request.placa()));
        vehiculo.setMarca(request.marca().trim());
        vehiculo.setModelo(request.modelo().trim());
        vehiculo.setAnio(request.anio());
        vehiculo.setColor(request.color());
        vehiculo.setVin(request.vin());
        vehiculo.setActivo(true);
        vehiculo.setCreateAt(LocalDateTime.now());
        return convertirADto(vehiculoRepository.save(vehiculo));
    }

    public VehiculoResponseDto getVehiculoById(Long id) {
        return convertirADto(buscarVehiculo(id));
    }

    public VehiculoResponseDto getVehiculoByPlaca(String placa){
        String placaNormalizada = PlacaUtil.normalizar(placa);
        return vehiculoRepository.findByPlaca(placaNormalizada)
                .map(this::convertirADto)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el vehículo con placa " + placaNormalizada));
    }

    public Optional<Vehiculo> buscarVehiculoPorPlaca(String placa) {
        return vehiculoRepository.findByPlaca(PlacaUtil.normalizar(placa));
    }

    public List<VehiculoResponseDto> getAllVehiculo() {
        return vehiculoRepository.findByActivoTrue().stream().map(this::convertirADto).toList();
    }

    public List<VehiculoResponseDto> getAllVehiculoDesactivado() {
        return vehiculoRepository.findByActivoFalse().stream().map(this::convertirADto).toList();
    }

    public void deleteVehiculoById(Long id) {
        Vehiculo vehiculo = buscarVehiculo(id);
        vehiculo.setActivo(false);
        vehiculo.setUpdateAt(LocalDateTime.now());
        vehiculoRepository.save(vehiculo);
    }

    public VehiculoResponseDto reactivarVehiculo(Long id) {
        Vehiculo vehiculo = buscarVehiculo(id);
        vehiculo.setActivo(true);
        vehiculo.setUpdateAt(LocalDateTime.now());
        return convertirADto(vehiculoRepository.save(vehiculo));
    }

    public Vehiculo buscarVehiculo(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el vehículo con id " + id));
    }

    public VehiculoResponseDto convertirADto(Vehiculo vehiculo) {
        return new VehiculoResponseDto(
                vehiculo.getIdVehiculo(),
                clienteService.convertirADto(vehiculo.getCliente()),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getColor(),
                vehiculo.getVin(),
                vehiculo.getActivo(),
                vehiculo.getCreateAt(),
                vehiculo.getUpdateAt()
        );
    }

}
