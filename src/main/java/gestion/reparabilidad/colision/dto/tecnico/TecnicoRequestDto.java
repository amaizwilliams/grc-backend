package gestion.reparabilidad.colision.dto.tecnico;

import gestion.reparabilidad.colision.modelo.Enums.Especialidad;
import jakarta.validation.constraints.*;

public record TecnicoRequestDto(

        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = "^\\d{6,10}$", message = "El documento debe tener solo números, entre 6 y 10 dígitos")
        String documento,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo puede contener letras y espacios")
        String nombreCompleto,

        @NotBlank(message = "El celular es obligatorio")
        @Pattern(regexp = "^(?=(?:\\D*\\d){10,15}\\D*$)\\+?\\d[\\d -]*\\d$",
                message = "El celular debe tener entre 10 y 15 dígitos (ej: 3001112233 o +57 300 111 2233)")
        String celular,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String correo,

        @NotNull(message = "La especialidad es obligatoria")
        Especialidad especialidad
) {
}
