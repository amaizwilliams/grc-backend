package gestion.reparabilidad.colision.dto.ordenReparacion;

import jakarta.validation.constraints.Size;

public record EntregaRequestDto(
        @Size(max = 500, message = "La nota no puede superar 500 caracteres")
        String nota,

        Boolean visibleCliente
)
{ }
