package cl.duoc.solicitudesMS.controller;

import cl.duoc.solicitudesMS.dto.CrearSolicitudDTO;
import cl.duoc.solicitudesMS.dto.RegistrarResultadoDTO;
import cl.duoc.solicitudesMS.dto.SolicitudConsultaDTO;
import cl.duoc.solicitudesMS.dto.SolicitudCreadaDTO;
import cl.duoc.solicitudesMS.model.TipoResiduo;
import cl.duoc.solicitudesMS.service.SolicitudRetiroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudRetiroController {

    private final SolicitudRetiroService service;

    public SolicitudRetiroController(SolicitudRetiroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SolicitudCreadaDTO> crear(
            @Valid @RequestBody CrearSolicitudDTO datos,
            Authentication authentication
    ) {
        verificarPropietario(datos.getVecinoId(), authentication);
        SolicitudCreadaDTO respuesta = service.crearSolicitud(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/vecino/{vecinoId}")
    public List<SolicitudConsultaDTO> consultarPorVecino(
            @PathVariable String vecinoId,
            Authentication authentication
    ) {
        verificarPropietario(vecinoId, authentication);
        return service.consultarSolicitudes(vecinoId);
    }

    private void verificarPropietario(String vecinoId, Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Long usuarioId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Inicia sesión de nuevo para identificar tu usuario");
        }
        if (!usuarioId.toString().equals(vecinoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes acceder a tus propias solicitudes");
        }
    }

    @GetMapping("/tipos-residuo")
    public List<TipoResiduo> listarTiposResiduo() {
        return service.listarTiposResiduo();
    }

    @PostMapping("/{folio}/resultado")
    public ResponseEntity<Void> registrarResultado(
            @PathVariable String folio,
            @Valid @RequestBody RegistrarResultadoDTO datos
    ) {
        service.registrarResultado(folio, datos.getResultado());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{folio}/asignacion")
    public ResponseEntity<Void> marcarComoAsignada(
            @PathVariable String folio
    ) {
        service.marcarComoAsignada(folio);
        return ResponseEntity.noContent().build();
    }
}
