package gestion.reparabilidad.colision.dto.tecnico;

import gestion.reparabilidad.colision.modelo.Enums.Especialidad;

import java.time.LocalDateTime;

public record TecnicoResponseDto(
        Long idPersona,
        String documento,
        String nombreCompleto,
        String celular,
        String correo,
        Especialidad especialidad,
        Boolean activo,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}
