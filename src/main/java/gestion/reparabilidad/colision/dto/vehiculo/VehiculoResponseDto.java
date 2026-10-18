package gestion.reparabilidad.colision.dto.vehiculo;

import gestion.reparabilidad.colision.dto.cliente.ClienteResponseDto;

import java.time.LocalDateTime;

public record VehiculoResponseDto(
        Long idVehiculo,
        ClienteResponseDto cliente,
        String placa,
        String marca,
        String modelo,
        Short anio,
        String color,
        String vin,
        Boolean activo,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}
