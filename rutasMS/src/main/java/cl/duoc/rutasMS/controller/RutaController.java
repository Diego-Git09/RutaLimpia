package cl.duoc.rutasMS.controller;

import cl.duoc.rutasMS.model.Ruta;
import cl.duoc.rutasMS.service.RutaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping
    public List<Ruta> listarRutas() {
        return rutaService.listarRutas();
    }

    @GetMapping("/{id}")
    public Ruta buscarRuta(@PathVariable("id") Long id) {
        return rutaService.buscarRuta(id);
    }

    @GetMapping("/camion/{idCamion}")
    public List<Ruta> listarPorCamion(
            @PathVariable("idCamion") Long idCamion) {
        return rutaService.listarPorCamion(idCamion);
    }

    @GetMapping("/conductor/{idConductor}")
    public List<Ruta> listarPorConductor(
            @PathVariable("idConductor") Long idConductor) {
        return rutaService.listarPorConductor(idConductor);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ruta registrarRuta(@RequestBody Ruta ruta) {
        return rutaService.registrarRuta(ruta);
    }

    @PutMapping("/{id}")
    public Ruta actualizarRuta(
            @PathVariable("id") Long id,
            @RequestBody Ruta ruta) {
        return rutaService.actualizarRuta(id, ruta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarRuta(@PathVariable("id") Long id) {
        rutaService.eliminarRuta(id);
    }
}