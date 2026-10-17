package gestion.reparabilidad.colision.dto.vehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class VehiculoRequestDto {

    @NotNull(message = "El cliente es obligatorio")
    private Long idCliente;

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Za-z]{3}[0-9]{2}[0-9A-Za-z]$", message = "Placa inválida (ej: ABC123 o ABC12D)")
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 20, message = "La marca no puede superar 20 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 10, message = "El modelo no puede superar 10 caracteres")
    private String modelo;

    private Short anio;

    @Size(max = 50, message = "El color no puede superar 50 caracteres")
    private String color;

    @Size(max = 100, message = "El VIN no puede superar 100 caracteres")
    private String vin;
}
