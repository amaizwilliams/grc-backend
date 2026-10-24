package gestion.reparabilidad.colision.dto.observacion;

import gestion.reparabilidad.colision.modelo.Enums.Repuestos;
import gestion.reparabilidad.colision.modelo.Enums.Ubicacion;

import java.time.LocalDateTime;

public record ObservacionResponseDto(
        Long idObservacion,
        Long idOrdenReparacion,
        Long idEtapa,                       // null = Entrega
        String nombreEtapa,                 // "Entrega" cuando idEtapa es null
        String autor,                       // el nombre, NUNCA la entidad Usuario (tiene passwordHash)
        String nota,
        Ubicacion ubicacion,
        Repuestos estadoRepuestos,
        Boolean visibleCliente,
        Integer cantidadImagenes,
        LocalDateTime createAt
)
{ }
