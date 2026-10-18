package gestion.reparabilidad.colision.modelo;

import gestion.reparabilidad.colision.modelo.Enums.Estado;
import gestion.reparabilidad.colision.modelo.Enums.Ubicacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "orden_reparacion")
public class OrdenReparacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_reparacion")
    private Long idOrdenReparacion;

    @ManyToOne
    @JoinColumn(name = "id_vehiculo", referencedColumnName = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_persona", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuarioCreador;

    @ManyToOne
    @JoinColumn(name = "id_etapa_actual", referencedColumnName = "id_etapa")
    private Etapas etapaActual;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", length = 20, nullable = false)
    private Estado estado = Estado.ACTIVA;

    @Column(name = "fecha_ingreso_cotizar")
    private LocalDate fechaIngresoCotizar;

    @Column(name = "fecha_ingreso_reparacion")
    private LocalDate fechaIngresoReparacion;

    @Column(name = "fecha_de_entrega_estimada")
    private LocalDate fechaDeEntregaEstimada;

    @Column(name = "fecha_de_entrega_real")
    private LocalDate fechaDeEntregaReal;

    @Column(name = "dias_estimado_entrega")
    private Byte diasEstimadoEntrega;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "ordenReparacion")
    private List<HistorialEtapas> historialEtapas = new ArrayList<>();

    @OneToMany(mappedBy = "ordenReparacion")
    private List<OrdenEtapaFecha> ordenEtapaFechas = new ArrayList<>();

    @OneToOne(mappedBy = "ordenReparacion")
    private Valoracion valoracion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ubicacion_actual", length = 20)
    private Ubicacion ubicacionActual;


}
