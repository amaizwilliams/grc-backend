package gestion.reparabilidad.colision.dto.cliente;

import java.time.LocalDateTime;

public record ClienteResponseDto(
        Long idPersona,
        String documento,
        String nombreCompleto,
        String celular,
        String correo,
        Boolean activo,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}
