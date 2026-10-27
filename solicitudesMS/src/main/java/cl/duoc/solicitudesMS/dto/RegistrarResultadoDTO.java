package cl.duoc.solicitudesMS.dto;

import cl.duoc.solicitudesMS.model.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistrarResultadoDTO {

    @NotNull
    private EstadoSolicitud resultado;
}