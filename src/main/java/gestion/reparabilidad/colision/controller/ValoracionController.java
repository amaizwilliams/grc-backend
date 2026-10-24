package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.valoracion.ValoracionRequestDto;
import gestion.reparabilidad.colision.dto.valoracion.ValoracionResponseDto;
import gestion.reparabilidad.colision.service.ValoracionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    @PostMapping("/api/orden/{idOrden}/valoracion")
    public ResponseEntity<ValoracionResponseDto> crearValoracion(@PathVariable Long idOrden, @Valid @RequestBody ValoracionRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(valoracionService.crearValoracion(idOrden,request));
    }

    @GetMapping("/api/orden/{idOrden}/valoracion")
    public ResponseEntity<ValoracionResponseDto> verValoracion(@PathVariable Long idOrden){
        return ResponseEntity.ok(valoracionService.obtenerValoracion(idOrden));
    }




}
