package gestion.reparabilidad.colision.dto.ordenReparacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEtapaRequestDto(
        @NotNull(message = "La etapa destino es obligatoria")
        Long idEtapaDestino,

        Long idTecnico,

        @Size(max = 500, message = "La nota no puede superar 500 caracteres")
        String nota,

        Boolean visibleCliente
)
{ }
