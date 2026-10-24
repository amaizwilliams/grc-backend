package gestion.reparabilidad.colision.dto.ordenReparacion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DiasEstimadosRequestDto(
        @NotNull(message = "Los días estimados son obligatorios")
        @Min(value = 1, message = "Los días estimados deben ser al menos 1")
        @Max(value = 30, message = "Los días estimados no pueden superar 30")
        Integer dias
) {
}
