package gestion.reparabilidad.colision.modelo;

import gestion.reparabilidad.colision.modelo.Enums.Especialidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "etapas")
public class Etapas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etapa")
    private Long idEtapa;

    @Column(name = "nombre_etapa", length = 100, nullable = false)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "dias_limite_critico")
    private Short diasLimiteCritico;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "especialidad", length = 20)
    private Especialidad especialidad;

}
