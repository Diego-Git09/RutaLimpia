package cl.duoc.rutasMS.dto;

import cl.duoc.rutasMS.model.Parada;
import java.time.LocalDateTime;

public record ParadaResponse(
        Long idParada,
        Long idRuta,
        String folio,
        String direccion,
        Integer orden,
        String estado,
        String observacion,
        LocalDateTime fechaResultado) {

    public static ParadaResponse desde(Parada parada) {
        return new ParadaResponse(
                parada.getIdParada(),
                parada.getRuta().getIdRuta(),
                parada.getFolio(),
                parada.getDireccion(),
                parada.getOrden(),
                parada.getEstado(),
                parada.getObservacion(),
                parada.getFechaResultado());
    }
}