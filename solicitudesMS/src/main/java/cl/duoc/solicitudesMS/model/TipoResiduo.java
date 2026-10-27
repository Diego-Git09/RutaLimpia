package cl.duoc.solicitudesMS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_residuo")
@Getter
@Setter
@NoArgsConstructor
public class TipoResiduo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long id;

    @Column(name = "tipo_residuo", nullable = false, unique = true, length = 60)
    private String nombre;
}