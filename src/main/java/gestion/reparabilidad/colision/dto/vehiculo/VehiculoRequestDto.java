package gestion.reparabilidad.colision.dto.vehiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VehiculoRequestDto(

        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^\\s*[A-Za-z]{3}[ -]?[0-9]{2}[0-9A-Za-z]\\s*$", message = "Placa inválida (ej: ABC123, ABC 123, ABC-123 o ABC12D)")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 20, message = "La marca no puede superar 20 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 10, message = "El modelo no puede superar 10 caracteres")
        String modelo,

        Short anio,

        @Size(max = 50, message = "El color no puede superar 50 caracteres")
        String color,

        @Size(max = 100, message = "El VIN no puede superar 100 caracteres")
        String vin
) {
}
