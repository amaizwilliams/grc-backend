package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.cliente.ClienteRequestDto;
import gestion.reparabilidad.colision.dto.cliente.ClienteUpdateDto;
import gestion.reparabilidad.colision.modelo.Cliente;
import gestion.reparabilidad.colision.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody ClienteRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.saveCliente(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteUpdateDto datos){
        return ResponseEntity.ok(clienteService.updateCliente(id, datos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getClienteById(id));
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<Cliente> buscarPorDocumento(@PathVariable String documento){
        return ResponseEntity.ok(clienteService.getClienteByDocumento(documento));
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.getAllCliente());
    }

    @GetMapping("/desactivados")
    public ResponseEntity<List<Cliente>> listarDesactivados() {
        return ResponseEntity.ok(clienteService.getAllClienteDesactivado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.deleteClienteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Cliente> reactivar(@PathVariable Long id){
        return ResponseEntity.ok(clienteService.reactivarCliente(id));

}
}

