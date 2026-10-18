package gestion.reparabilidad.colision.dto.ordenReparacion;


import jakarta.validation.constraints.NotNull;

public record OrdenReparacionRequestDto(
        @NotNull(message = "El vehículo es obligatorio")
        Long idVehiculo,
        @NotNull(message = "El cliente es obligatorio")
        Long idCliente

)
{}


