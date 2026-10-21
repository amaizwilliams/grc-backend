package gestion.reparabilidad.colision.dto.etapas;

import gestion.reparabilidad.colision.modelo.Enums.Especialidad;

public record EtapasResponseDto(

        Long idEtapa,
        String nombre,
        Integer orden,
        String descripcion,
        Short diasLimiteCritico,
        Especialidad especialidad
)
{
}
