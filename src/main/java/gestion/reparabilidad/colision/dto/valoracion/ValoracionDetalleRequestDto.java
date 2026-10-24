package gestion.reparabilidad.colision.dto.valoracion;

import gestion.reparabilidad.colision.modelo.Enums.Accion;
import gestion.reparabilidad.colision.modelo.Enums.Gravedad;
import jakarta.validation.constraints.*;

public record ValoracionDetalleRequestDto(
        @NotBlank(message = "El nombre de la pieza es obligatorio")
        @Size(max = 255, message = "El nombre de la pieza no puede superar 255 caracteres")
        String pieza,

        @NotNull(message = "La acción es obligatoria (REPARACION o SUSTITUCION)")
        Accion accion,

        Gravedad gravedad,// obligatoria con REPARACION, null con SUSTITUCION (se valida en el Service)

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 999, message = "La cantidad no puede superar 999")
        Integer cantidad,                   // opcional: si no viene, se guarda 1

        @Size(max = 255, message = "La observación no puede superar 255 caracteres")
        String observacion
)
{ }
