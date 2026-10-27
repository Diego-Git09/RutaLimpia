package cl.duoc.solicitudesMS.dto;

import cl.duoc.solicitudesMS.model.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudConsultaDTO {
    private String folio;
    private String direccion;
    private String tipoResiduo;
    private Integer cantidadKg;
    private LocalDateTime horarioPreferido;
    private EstadoSolicitud estado;
}
