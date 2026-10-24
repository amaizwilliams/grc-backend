package gestion.reparabilidad.colision.dto.valoracion;

import java.time.LocalDateTime;
import java.util.List;

public record ValoracionResponseDto(
        Long idValoracion,
        Long idOrdenReparacion,
        String placa,
        String descripcionGeneral,
        String autor,                       // el nombre, NUNCA la entidad Usuario (tiene passwordHash)
        Boolean cargadaCesvi,
        LocalDateTime cargadaCesviAt,
        LocalDateTime createAt,
        LocalDateTime updateAt,
        List<ValoracionDetalleResponseDto> detalles,   // SUSTITUCION primero, alfabético por pieza
        Integer cantidadImagenes
)
{ }
