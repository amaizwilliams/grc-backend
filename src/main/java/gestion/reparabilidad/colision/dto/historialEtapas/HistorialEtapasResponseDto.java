package gestion.reparabilidad.colision.dto.historialEtapas;

import java.time.LocalDateTime;

public record HistorialEtapasResponseDto(
        Long idHistorialEtapas,
        Long idOrdenReparacion,
        Long idEtapa,
        String nombreEtapa,
        String usuario,                 // el nombre, NUNCA la entidad Usuario (tiene passwordHash)
        LocalDateTime fechaCambio
)
{ }
