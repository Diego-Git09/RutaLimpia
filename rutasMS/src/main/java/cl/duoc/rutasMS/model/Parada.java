package cl.duoc.rutasMS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "paradas", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_parada_folio",
                columnNames = "folio"),
        @UniqueConstraint(
                name = "uk_parada_ruta_orden",
                columnNames = {"id_ruta", "orden"})
})
@Getter
@Setter
@NoArgsConstructor
public class Parada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;

    @Column(name = "folio", nullable = false, length = 36)
    private String folio;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private String estado;

    @Column(length = 500)
    private String observacion;

    private LocalDateTime fechaResultado;
}