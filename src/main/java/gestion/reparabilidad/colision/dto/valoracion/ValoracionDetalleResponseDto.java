package gestion.reparabilidad.colision.dto.valoracion;

import gestion.reparabilidad.colision.modelo.Enums.Accion;
import gestion.reparabilidad.colision.modelo.Enums.Gravedad;

public record ValoracionDetalleResponseDto(
        Long idValoracionDetalle,
        String pieza,
        Accion accion,
        Gravedad gravedad,
        Short cantidad,
        String observacion
)
{}
