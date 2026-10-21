package gestion.reparabilidad.colision.dto.etapas;

import gestion.reparabilidad.colision.modelo.Enums.Especialidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EtapasRequestDto(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotNull(message = "El orden es obligatorio")
        @Positive(message = "El orden debe ser mayor que cero")
        Integer orden,

        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion,

        @Positive(message = "Los días límite deben ser mayores que cero")
        Short diasLimiteCritico,

        Especialidad especialidad
)
{ }
