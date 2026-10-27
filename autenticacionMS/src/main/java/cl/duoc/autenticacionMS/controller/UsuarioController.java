package cl.duoc.autenticacionMS.controller;

import cl.duoc.autenticacionMS.dto.CrearVecinoDTO;
import cl.duoc.autenticacionMS.dto.UsuarioCreadoDTO;
import cl.duoc.autenticacionMS.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/vecinos")
    public ResponseEntity<UsuarioCreadoDTO> registrarVecino(
            @Valid @RequestBody CrearVecinoDTO datos
    ) {
        UsuarioCreadoDTO respuesta = usuarioService.registrarVecino(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/perfil")
    public UsuarioCreadoDTO consultarPerfil(Authentication authentication) {
        // Spring obtiene el correo de la identidad que guardó el filtro JWT.
        return usuarioService.consultarPerfil(authentication.getName());
    }
}