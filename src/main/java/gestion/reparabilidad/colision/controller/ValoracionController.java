package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.ordenReparacion.OrdenReparacionResponseDto;
import gestion.reparabilidad.colision.dto.valoracion.ConteoValoracionResponseDto;
import gestion.reparabilidad.colision.dto.valoracion.ValoracionRequestDto;
import gestion.reparabilidad.colision.dto.valoracion.ValoracionResponseDto;
import gestion.reparabilidad.colision.modelo.Enums.FiltroValoracion;
import gestion.reparabilidad.colision.service.ValoracionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/api/orden/{idOrden}/valoracion")
    public ResponseEntity<ValoracionResponseDto> editarValoracion(@PathVariable Long idOrden, @Valid @RequestBody ValoracionRequestDto request){
        return ResponseEntity.ok(valoracionService.editarValoracion(idOrden, request));
    }

    @GetMapping("/api/orden/{idOrden}/valoracion")
    public ResponseEntity<ValoracionResponseDto> verValoracion(@PathVariable Long idOrden){
        return ResponseEntity.ok(valoracionService.obtenerValoracion(idOrden));
    }

    @PatchMapping("/api/orden/{idOrden}/valoracion/cesvi")
    public  ResponseEntity<ValoracionResponseDto> marcarCargadaCesvi(@PathVariable Long idOrden){
        return ResponseEntity.ok(valoracionService.marcarCargadaCesvi(idOrden));
    }

    @GetMapping("/api/valoracion/ordenes")
    public ResponseEntity<List<OrdenReparacionResponseDto>> listarOrdenesPorFiltro(@RequestParam FiltroValoracion filtro) {
        return ResponseEntity.ok(valoracionService.listarOrdenesPorFiltro(filtro));
    }

    @GetMapping("/api/valoracion/conteo")
    public ResponseEntity<ConteoValoracionResponseDto> contarPorFiltro() {
        return ResponseEntity.ok(valoracionService.contarPorFiltro());
    }




}
