package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.ordenReparacion.*;
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

    @PatchMapping("/{id}/ingreso-cotizar")
    public ResponseEntity<OrdenReparacionResponseDto> marcarFechaCotizar(@PathVariable Long id, @Valid @RequestBody FechaRequestDto request) {
        return ResponseEntity.ok(ordenReparacionService.marcarIngresoCotizar(id, request));
    }

    @PatchMapping("/{id}/ingreso-reparacion")
    public ResponseEntity<OrdenReparacionResponseDto> marcarFechaIngresoReparacion(@PathVariable Long id, @Valid @RequestBody FechaRequestDto requestDto) {
        return ResponseEntity.ok(ordenReparacionService.marcarIngresoTaller(id, requestDto));
    }

    @PatchMapping("/{id}/dias-estimados")
    public ResponseEntity<OrdenReparacionResponseDto> diasEstimadosReparacion(@PathVariable Long id, @Valid @RequestBody DiasEstimadosRequestDto request) {
        return ResponseEntity.ok(ordenReparacionService.marcarDiasEstimados(id, request));
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

    @GetMapping("/entregadas")
    public ResponseEntity<List<OrdenReparacionResponseDto>> listarEntregadas() {
        return ResponseEntity.ok(ordenReparacionService.getOrdenesEntregadas());
    }

    @GetMapping
    public ResponseEntity<List<OrdenReparacionResponseDto>> listarActivas() {
        return ResponseEntity.ok(ordenReparacionService.getAllOrdenActivas());
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<List<OrdenReparacionResponseDto>> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(ordenReparacionService.getOrdenesByPlaca(placa));
    }

    @PatchMapping("/{id}/etapa")
    public ResponseEntity<OrdenReparacionResponseDto> cambiarEtapa(@PathVariable Long id, @Valid @RequestBody CambioEtapaRequestDto request) {
        return ResponseEntity.ok(ordenReparacionService.cambiarEtapa(id, request));
    }

    @PatchMapping("/{id}/entregar-orden")
    public ResponseEntity<OrdenReparacionResponseDto> entregaVehiculo(@PathVariable Long id, @Valid @RequestBody EntregaRequestDto request){
        return ResponseEntity.ok(ordenReparacionService.entregarOrden(id, request));
    }
}
