package cl.duoc.autenticacionMS.controller;

import cl.duoc.autenticacionMS.dto.LoginDTO;
import cl.duoc.autenticacionMS.dto.RespuestaLoginDTO;
import cl.duoc.autenticacionMS.service.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/login")
    public ResponseEntity<RespuestaLoginDTO> iniciarSesion(
            @Valid @RequestBody LoginDTO datos
    ) {
        return ResponseEntity.ok(autenticacionService.iniciarSesion(datos));
    }
}