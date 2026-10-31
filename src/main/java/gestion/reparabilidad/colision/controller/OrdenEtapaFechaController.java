package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.ordenEtapaFecha.OrdenEtapaFechaResponseDto;
import gestion.reparabilidad.colision.service.OrdenEtapaFechaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orden/{idOrden}/etapas")
public class OrdenEtapaFechaController {

    private final OrdenEtapaFechaService ordenEtapaFechaService;

    public OrdenEtapaFechaController(OrdenEtapaFechaService ordenEtapaFechaService) {
        this.ordenEtapaFechaService = ordenEtapaFechaService;
    }

    @GetMapping
    public ResponseEntity<List<OrdenEtapaFechaResponseDto>> listarEtapasOrden(@PathVariable Long idOrden) {
        return ResponseEntity.ok(ordenEtapaFechaService.listarEtapasOrden(idOrden));
    }
}
