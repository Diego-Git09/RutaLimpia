package cl.duoc.autenticacionMS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UsuarioCreadoDTO {

    private final Long id;
    private final String nombre;
    private final String email;
    private final String rol;
}
