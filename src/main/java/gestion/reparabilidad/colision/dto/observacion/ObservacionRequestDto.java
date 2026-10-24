package gestion.reparabilidad.colision.dto.observacion;

import gestion.reparabilidad.colision.modelo.Enums.Repuestos;
import gestion.reparabilidad.colision.modelo.Enums.Ubicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObservacionRequestDto(
        Long idEtapa,                       // null = "Entrega" (solo en órdenes CERRADA)

        @NotBlank(message = "La nota es obligatoria")
        @Size(max = 500, message = "La nota no puede superar 500 caracteres")
        String nota,

        Ubicacion ubicacion,                // opcional: si viene, actualiza la ubicación de la orden
        Repuestos estadoRepuestos,
        Boolean visibleCliente
)
{ }
