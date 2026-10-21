package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.etapas.EtapasRequestDto;
import gestion.reparabilidad.colision.dto.etapas.EtapasResponseDto;
import gestion.reparabilidad.colision.service.EtapasService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etapa")
public class EtapasController {

    private final EtapasService etapasService;

    public EtapasController(EtapasService etapasService) {
        this.etapasService = etapasService;
    }

    @GetMapping
    public ResponseEntity<List<EtapasResponseDto>> listar() {
        return ResponseEntity.ok(etapasService.getAllEtapas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtapasResponseDto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(etapasService.getEtapaById(id));
    }

    @PostMapping
    public ResponseEntity<EtapasResponseDto> crear(@Valid @RequestBody EtapasRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(etapasService.saveEtapa(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EtapasResponseDto> actualizar(@PathVariable Long id, @Valid @RequestBody EtapasRequestDto request) {
        return ResponseEntity.ok(etapasService.updateEtapa(id, request));
    }
}
