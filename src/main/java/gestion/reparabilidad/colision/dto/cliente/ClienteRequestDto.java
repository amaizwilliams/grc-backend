package gestion.reparabilidad.colision.dto.cliente;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ClienteRequestDto {

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^\\d{6,10}$", message = "El documento debe tener solo números, entre 6 y 10 dígitos")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombreCompleto;

    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "^\\+?[0-9 -]{10,20}$", message = "El celular no tiene un formato válido")
    private String celular;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String correo;
    
}
