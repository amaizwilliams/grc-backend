package gestion.reparabilidad.colision.controller;

import gestion.reparabilidad.colision.dto.vehiculo.VehiculoRequestDto;
import gestion.reparabilidad.colision.dto.vehiculo.VehiculoResponseDto;
import gestion.reparabilidad.colision.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculo")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDto> buscar(@PathVariable Long id){
        return ResponseEntity.ok(vehiculoService.getVehiculoById(id));
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<VehiculoResponseDto> buscarVehiculoPlaca(@PathVariable String placa){
        return ResponseEntity.ok(vehiculoService.getVehiculoByPlaca(placa));
    }

    @GetMapping
    public  ResponseEntity<List<VehiculoResponseDto>> listar(){
        return  ResponseEntity.ok(vehiculoService.getAllVehiculo());
    }

    @GetMapping("/desactivados")
    public ResponseEntity<List<VehiculoResponseDto>> listarVehiculosDesactivados(){
        return ResponseEntity.ok(vehiculoService.getAllVehiculoDesactivado());
    }

    @PostMapping
    public  ResponseEntity<VehiculoResponseDto> crearVehiculo(@Valid @RequestBody VehiculoRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehiculoService.saveVehiculo(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        vehiculoService.deleteVehiculoById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<VehiculoResponseDto> reactivar(@PathVariable Long id){
        return ResponseEntity.ok(vehiculoService.reactivarVehiculo(id));
    }


}
