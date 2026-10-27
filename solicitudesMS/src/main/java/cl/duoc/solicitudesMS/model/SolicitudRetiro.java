package cl.duoc.solicitudesMS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_retiro")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String folio;

    @Column(nullable = false)
    private String vecinoId;

    @Column(nullable = false, length = 200)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoResiduo tipoResiduo;

    @Column(nullable = false)
    private Integer cantidadKg;

    @Column(nullable = false)
    private LocalDateTime horarioPreferido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @OneToMany(mappedBy = "solicitud")
    private List<HistorialEstado> historialEstados = new ArrayList<>();
}