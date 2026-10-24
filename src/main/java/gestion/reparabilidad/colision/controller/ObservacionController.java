package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.observacion.ObservacionRequestDto;
import gestion.reparabilidad.colision.dto.observacion.ObservacionResponseDto;
import gestion.reparabilidad.colision.service.ObservacionesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orden/{idOrden}/observaciones")
@CrossOrigin(origins = "*")
public class ObservacionController {
    private final ObservacionesService observacionesService;

    public ObservacionController(ObservacionesService observacionesService) {
        this.observacionesService = observacionesService;
    }

    @PostMapping
    public ResponseEntity<ObservacionResponseDto> crearObservacion(@PathVariable Long idOrden, @Valid @RequestBody ObservacionRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(observacionesService.crearObservacion(idOrden, request));
    }

    @GetMapping
    public  ResponseEntity<List<ObservacionResponseDto>> listarObservaciones(@PathVariable Long idOrden){
        return ResponseEntity.ok(observacionesService.listarObservaciones(idOrden));
    }


}

