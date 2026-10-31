package gestion.reparabilidad.colision.dto.ordenEtapaFecha;

import java.time.LocalDateTime;

public record OrdenEtapaFechaResponseDto(
        Long idOrdenEtapaFecha,
        Long idOrdenReparacion,
        Long idEtapa,
        String nombreEtapa,
        Long idTecnico,                 // null = etapa sin técnico (Bancada, Listo para entregar)
        String nombreTecnico,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,         // null = etapa en curso
        Boolean completada
)
{ }
