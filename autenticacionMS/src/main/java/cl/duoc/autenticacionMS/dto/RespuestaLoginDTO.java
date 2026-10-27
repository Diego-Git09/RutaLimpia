package cl.duoc.autenticacionMS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RespuestaLoginDTO {

    private final String token;
    private final String tipo;
}