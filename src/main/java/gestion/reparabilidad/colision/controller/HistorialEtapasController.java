package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.historialEtapas.HistorialEtapasResponseDto;
import gestion.reparabilidad.colision.service.HistorialEtapasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orden/{idOrden}/historial")
public class HistorialEtapasController {
    private final HistorialEtapasService historialEtapasService;

    public HistorialEtapasController(HistorialEtapasService historialEtapasService) {
        this.historialEtapasService = historialEtapasService;
    }

    @GetMapping
    public ResponseEntity<List<HistorialEtapasResponseDto>> listarHistorialEtapas(@PathVariable Long idOrden){
        return ResponseEntity.ok(historialEtapasService.listarHistorialEtapas(idOrden));
    }
}
