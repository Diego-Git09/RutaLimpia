package cl.duoc.rutasMS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rutas", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_ruta_camion_fecha",
                columnNames = {"id_camion", "fecha"}),
        @UniqueConstraint(
                name = "uk_ruta_conductor_fecha",
                columnNames = {"id_conductor", "fecha"})
})
@Getter
@Setter
@NoArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long idRuta;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "id_camion", nullable = false)
    private Long idCamion;

    @Column(name = "id_conductor", nullable = false)
    private Long idConductor;
}