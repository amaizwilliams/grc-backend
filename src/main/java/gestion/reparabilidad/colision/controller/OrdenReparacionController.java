package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.ordenReparacion.OrdenReparacionRequestDto;
import gestion.reparabilidad.colision.dto.ordenReparacion.OrdenReparacionResponseDto;
import gestion.reparabilidad.colision.service.OrdenReparacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orden")
@CrossOrigin(origins = "*")
public class OrdenReparacionController {

    private final OrdenReparacionService ordenReparacionService;

    public OrdenReparacionController(OrdenReparacionService ordenReparacionService) {
        this.ordenReparacionService = ordenReparacionService;
    }

    @PostMapping
    public ResponseEntity<OrdenReparacionResponseDto> crearOrden(@Valid @RequestBody OrdenReparacionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenReparacionService.saveOrden(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenReparacionResponseDto> actualizar(@PathVariable Long id, @Valid @RequestBody OrdenReparacionRequestDto datos) {
        return ResponseEntity.ok(ordenReparacionService.updateOrden(id, datos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenReparacionService.deleteOrdenById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<OrdenReparacionResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenReparacionService.reactivarOrden(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenReparacionResponseDto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenReparacionService.getOrdenById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrdenReparacionResponseDto>> listarActivas() {
        return ResponseEntity.ok(ordenReparacionService.getAllOrdenActivas());
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<List<OrdenReparacionResponseDto>> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(ordenReparacionService.getOrdenesByPlaca(placa));
    }

}
