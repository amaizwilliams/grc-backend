package gestion.reparabilidad.colision.dto.tecnico;
import gestion.reparabilidad.colision.modelo.Enums.Especialidad;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TecnicoRequestDto {

    /*
    ESTA VRGA LA DEJE YO MISMO, NO ESTOY HACIENDO VIBE CODING, ESTOY APRENDIENDO....

    Cómo leer las anotaciones:
    - @NotBlank: rechaza null, "" y "   ". Solo aplica a String.
    - @Pattern: la expresión regular tiene que cumplirse completa. \\d{6,10} = entre 6 y 10 dígitos. \\p{L} = cualquier letra, incluidas tildes y ñ (Pérez, Muñoz), algo que [a-zA-Z] no cubre. Ese patrón es el que rechaza el "1234" que tienes guardado.
    - En Java la barra invertida se escribe doble dentro de un String (\\d), porque \d solo no compila.
    */

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^\\d{6,10}$", message = "El documento debe tener solo números, entre 6 y 10 dígitos")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombreCompleto;

    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10,15}\\D*$)\\+?\\d[\\d -]*\\d$",
            message = "El celular debe tener entre 10 y 15 dígitos (ej: 3001112233 o +57 300 111 2233)")
    private String celular;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String correo;

    @NotNull(message = "La especialidad es obligatoria")
    private Especialidad especialidad;

}

