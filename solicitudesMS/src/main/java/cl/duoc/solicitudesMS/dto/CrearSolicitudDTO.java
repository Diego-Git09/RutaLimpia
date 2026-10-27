package cl.duoc.solicitudesMS.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CrearSolicitudDTO {

    @NotBlank
    private String vecinoId;

    @NotBlank
    private String direccion;

    @NotNull
    private Long tipoResiduoId;

    @NotNull
    private LocalDateTime horarioPreferido;

    @NotNull
    @Positive
    private Integer cantidadKg;
}
