package gestion.reparabilidad.colision.dto.ordenReparacion;

import gestion.reparabilidad.colision.dto.cliente.ClienteResponseDto;
import gestion.reparabilidad.colision.dto.vehiculo.VehiculoResponseDto;
import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.Enums.Ubicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrdenReparacionResponseDto(
                Long idOrdenReparacion,
                VehiculoResponseDto vehiculo,
                ClienteResponseDto cliente,
                String usuarioCreador,          // el nombre, NUNCA la entidad Usuario (tiene passwordHash)
                String asesor,                  // último HistorialEtapas.usuario; null = nunca cambió de etapa
                Long idEtapaActual,             // null = "Asignado"
                String nombreEtapaActual,
                Estado estado,
                Ubicacion ubicacionActual,
                LocalDate fechaIngresoCotizar,
                LocalDate fechaIngresoReparacion,
                LocalDate fechaDeEntregaEstimada,
                LocalDate fechaDeEntregaReal,
                Byte diasEstimadoEntrega,
                LocalDateTime createAt,
                LocalDateTime updateAt
)
{}
