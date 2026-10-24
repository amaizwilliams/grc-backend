package gestion.reparabilidad.colision.dto.valoracion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ValoracionRequestDto(
        @Size(max = 500, message = "La descripción general no puede superar 500 caracteres")
        String descripcionGeneral,

        @NotEmpty(message = "La valoración debe tener al menos una pieza")
        List<@Valid ValoracionDetalleRequestDto> detalles
)
{ }
