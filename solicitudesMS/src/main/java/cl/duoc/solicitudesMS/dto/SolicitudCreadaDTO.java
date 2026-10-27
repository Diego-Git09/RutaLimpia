package cl.duoc.solicitudesMS.dto;

import cl.duoc.solicitudesMS.model.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SolicitudCreadaDTO {

    private final String folio;
    private final EstadoSolicitud estado;
}