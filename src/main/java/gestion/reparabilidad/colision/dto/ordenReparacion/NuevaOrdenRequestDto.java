package gestion.reparabilidad.colision.dto.ordenReparacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Payload del wizard "Nueva orden": crea el cliente y/o el vehículo si vienen,
// y luego la orden, todo en una sola transacción.
public record NuevaOrdenRequestDto(

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^\\s*[A-Za-z]{3}[ -]?[0-9]{2}[0-9A-Za-z]\\s*$",
                message = "Placa inválida (ej: ABC123, ABC 123, ABC-123 o ABC12D)")
        String placa,

        Long idCliente,

        @Valid
        ClienteNuevoDto cliente,

        @Valid
        VehiculoNuevoDto vehiculo
) {

    public record ClienteNuevoDto(

            @NotBlank(message = "El documento es obligatorio")
            @Pattern(regexp = "^\\s*(?=(?:[^0-9]*[0-9]){6,10}[^0-9]*$)[0-9]+(?:[ .][0-9]+)*\\s*$",
                    message = "El documento debe tener entre 6 y 10 dígitos (ej: 1017171717 o 1.017.171.717)")
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
            String correo
    ) {
    }

    public record VehiculoNuevoDto(

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
}
