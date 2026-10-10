package gestion.reparabilidad.colision.service;
import gestion.reparabilidad.colision.dto.cliente.ClienteRequestDto;
import gestion.reparabilidad.colision.dto.cliente.ClienteUpdateDto;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Cliente;
import gestion.reparabilidad.colision.repository.ClienteRepository;
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

    public Cliente saveCliente(ClienteRequestDto request){
        String documento = request.getDocumento().trim();
        if (clienteRepository.existsByDocumento(documento)) {
            throw new ReglaNegocioException("Ya existe un cliente con el documento " + documento);
        }
        Cliente cliente = new Cliente();
        cliente.setDocumento(documento);
        cliente.setNombreCompleto(request.getNombreCompleto().trim());
        cliente.setCelular(request.getCelular());
        cliente.setCorreo(request.getCorreo());
        LocalDateTime ahora = LocalDateTime.now();
        cliente.setActivo(true);
        cliente.setCreateAt(ahora);
        cliente.setUpdateAt(ahora);
        return clienteRepository.save(cliente);
    }

    public Cliente updateCliente(Long id, ClienteUpdateDto datos){
        Cliente cliente = getClienteById(id);
        cliente.setNombreCompleto(datos.getNombreCompleto().trim());
        cliente.setCelular(datos.getCelular());
        cliente.setCorreo(datos.getCorreo());
        cliente.setUpdateAt(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    public Cliente getClienteById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
    }

    public Cliente getClienteByDocumento(String documento){
        String documentoNormalizado = documento.trim();
        return clienteRepository.findByDocumento(documentoNormalizado)
                .orElseThrow(()-> new RecursoNoEncontradoException("No hay clientes registrados con este documento: "+documentoNormalizado));
    }

    public List<Cliente> getAllCliente() {
        return clienteRepository.findByActivoTrue();
    }

    public List<Cliente> getAllClienteDesactivado() {
        return clienteRepository.findByActivoFalse();
    }

    public void deleteClienteById(Long id) {
        Cliente cliente = getClienteById(id);
        cliente.setActivo(false);
        cliente.setUpdateAt(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    public Cliente reactivarCliente(Long id){
        Cliente cliente = getClienteById(id);
        cliente.setActivo(true);
        cliente.setUpdateAt(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

}
