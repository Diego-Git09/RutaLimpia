package cl.duoc.rutasMS.controller;

import cl.duoc.rutasMS.dto.*;
import cl.duoc.rutasMS.service.ParadaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas/{idRuta}/paradas")
public class ParadaController {

    private final ParadaService paradaService;

    public ParadaController(ParadaService paradaService) {
        this.paradaService = paradaService;
    }

    @GetMapping
    public List<ParadaResponse> listarParadas(
            @PathVariable("idRuta") Long idRuta) {
        return paradaService.listarParadas(idRuta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParadaResponse agregarParada(
            @PathVariable("idRuta") Long idRuta,
            @RequestBody CrearParadaRequest datos) {
        return paradaService.agregarParada(idRuta, datos);
    }

    @PatchMapping("/{idParada}/resultado")
    public ParadaResponse registrarResultado(
            @PathVariable("idRuta") Long idRuta,
            @PathVariable("idParada") Long idParada,
            @RequestBody ResultadoParadaRequest datos) {
        return paradaService.registrarResultado(
                idRuta, idParada, datos);
    }
}