package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.cliente.ClienteRequestDto;
import gestion.reparabilidad.colision.dto.cliente.ClienteUpdateDto;
import gestion.reparabilidad.colision.dto.cliente.ClienteResponseDto;
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
    public ResponseEntity<ClienteResponseDto> crearCliente(@Valid @RequestBody ClienteRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.saveCliente(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteUpdateDto datos){
        return ResponseEntity.ok(clienteService.updateCliente(id, datos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getClienteById(id));
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ClienteResponseDto> buscarPorDocumento(@PathVariable String documento){
        return ResponseEntity.ok(clienteService.getClienteByDocumento(documento));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> listar() {
        return ResponseEntity.ok(clienteService.getAllCliente());
    }

    @GetMapping("/desactivados")
    public ResponseEntity<List<ClienteResponseDto>> listarDesactivados() {
        return ResponseEntity.ok(clienteService.getAllClienteDesactivado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.deleteClienteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ClienteResponseDto> reactivar(@PathVariable Long id){
        return ResponseEntity.ok(clienteService.reactivarCliente(id));
    }

}

