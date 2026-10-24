package gestion.reparabilidad.colision.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "valoracion")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private Long idValoracion;

    @OneToOne
    @JoinColumn(name = "id_orden_reparacion", referencedColumnName = "id_orden_reparacion", nullable = false, unique = true)
    private OrdenReparacion ordenReparacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "descripcion_general", length = 500)
    private String descripcionGeneral;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "cargada_cesvi", nullable = false)
    private Boolean cargadaCesvi = false;

    @Column(name = "cargada_cesvi_at")
    private LocalDateTime cargadaCesviAt;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "valoracion")
    private List<ValoracionDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "valoracion")
    private List<ValoracionImagen> imagenes = new ArrayList<>();

}
