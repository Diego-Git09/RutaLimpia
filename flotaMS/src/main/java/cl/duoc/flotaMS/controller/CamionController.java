package cl.duoc.flotaMS.controller;

import cl.duoc.flotaMS.model.Camion;
import cl.duoc.flotaMS.service.CamionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    public List<Camion> listarCamiones() {
        return camionService.listarCamiones();
    }

    @GetMapping("/disponibles")
    public List<Camion> listarDisponibles() {
        return camionService.listarDisponibles();
    }

    @GetMapping("/{id}")
    public Camion buscarCamion(@PathVariable("id") Long id) {
        return camionService.buscarCamion(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Camion registrarCamion(@RequestBody Camion camion) {
        return camionService.registrarCamion(camion);
    }

    @PutMapping("/{id}")
    public Camion actualizarCamion(
            @PathVariable("id") Long id,
            @RequestBody Camion camion) {
        return camionService.actualizarCamion(id, camion);
    }

    @PatchMapping("/{id}/estado")
    public Camion cambiarEstado(
            @PathVariable("id") Long id,
            @RequestBody CambioEstado datos) {
        return camionService.cambiarEstado(id, datos.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCamion(@PathVariable("id") Long id) {
        camionService.eliminarCamion(id);
    }

    public record CambioEstado(String estado) {
    }
}