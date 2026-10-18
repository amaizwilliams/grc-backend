package gestion.reparabilidad.colision.service;
import gestion.reparabilidad.colision.dto.cliente.ClienteRequestDto;
import gestion.reparabilidad.colision.dto.cliente.ClienteResponseDto;
import gestion.reparabilidad.colision.dto.cliente.ClienteUpdateDto;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Cliente;
import gestion.reparabilidad.colision.repository.ClienteRepository;
import gestion.reparabilidad.colision.util.CelularUtil;
import gestion.reparabilidad.colision.util.DocumentoUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponseDto saveCliente(ClienteRequestDto request){
        String documento = DocumentoUtil.normalizar(request.documento());
        if (clienteRepository.existsByDocumento(documento)) {
            throw new ReglaNegocioException("Ya existe un cliente con el documento " + documento);
        }
        Cliente cliente = new Cliente();
        cliente.setDocumento(documento);
        cliente.setNombreCompleto(request.nombreCompleto().trim());
        cliente.setCelular(CelularUtil.normalizar(request.celular()));
        cliente.setCorreo(request.correo());
        cliente.setActivo(true);
        cliente.setCreateAt(LocalDateTime.now());
        return convertirADto(clienteRepository.save(cliente));
    }

    public ClienteResponseDto updateCliente(Long id, ClienteUpdateDto datos){
        Cliente cliente = buscarCliente(id);
        cliente.setNombreCompleto(datos.nombreCompleto().trim());
        cliente.setCelular(CelularUtil.normalizar(datos.celular()));
        cliente.setCorreo(datos.correo());
        cliente.setUpdateAt(LocalDateTime.now());
        return convertirADto(clienteRepository.save(cliente));
    }

    public ClienteResponseDto getClienteById(Long id) {
        return convertirADto(buscarCliente(id));
    }

    public ClienteResponseDto getClienteByDocumento(String documento){
        String documentoNormalizado = DocumentoUtil.normalizar(documento);
        return clienteRepository.findByDocumento(documentoNormalizado)
                .map(this::convertirADto)
                .orElseThrow(()-> new RecursoNoEncontradoException("No hay clientes registrados con este documento: "+documentoNormalizado));
    }

    public List<ClienteResponseDto> getAllCliente() {
        return clienteRepository.findByActivoTrue().stream().map(this::convertirADto).toList();
    }

    public List<ClienteResponseDto> getAllClienteDesactivado() {
        return clienteRepository.findByActivoFalse().stream().map(this::convertirADto).toList();
    }

    public void deleteClienteById(Long id) {
        Cliente cliente = buscarCliente(id);
        cliente.setActivo(false);
        cliente.setUpdateAt(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    public ClienteResponseDto reactivarCliente(Long id){
        Cliente cliente = buscarCliente(id);
        cliente.setActivo(true);
        cliente.setUpdateAt(LocalDateTime.now());
        return convertirADto(clienteRepository.save(cliente));
    }

    // Públicos porque VehiculoService los usa: buscar el dueño y anidarlo en su respuesta
    public Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
    }

    public ClienteResponseDto convertirADto(Cliente cliente) {
        return new ClienteResponseDto(
                cliente.getIdPersona(),
                cliente.getDocumento(),
                cliente.getNombreCompleto(),
                cliente.getCelular(),
                cliente.getCorreo(),
                cliente.getActivo(),
                cliente.getCreateAt(),
                cliente.getUpdateAt()
        );
    }

}
