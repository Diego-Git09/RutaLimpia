package cl.duoc.rutasMS.service;

import cl.duoc.rutasMS.client.ResultadoSolicitudRequest;
import cl.duoc.rutasMS.client.SolicitudesClient;
import cl.duoc.rutasMS.dto.*;
import cl.duoc.rutasMS.model.Parada;
import cl.duoc.rutasMS.model.Ruta;
import cl.duoc.rutasMS.repository.ParadaRepository;
import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ParadaService {

    private final ParadaRepository paradaRepository;
    private final RutaService rutaService;
    private final SolicitudesClient solicitudesClient;

    public ParadaService(
            ParadaRepository paradaRepository,
            RutaService rutaService,
            SolicitudesClient solicitudesClient) {
        this.paradaRepository = paradaRepository;
        this.rutaService = rutaService;
        this.solicitudesClient = solicitudesClient;
    }

    @Transactional(readOnly = true)
    public List<ParadaResponse> listarParadas(Long idRuta) {
        rutaService.buscarRuta(idRuta);

        return paradaRepository
                .findByRuta_IdRutaOrderByOrdenAsc(idRuta)
                .stream()
                .map(ParadaResponse::desde)
                .toList();
    }

    public ParadaResponse agregarParada(
            Long idRuta, CrearParadaRequest datos) {

        Ruta ruta = rutaService.buscarRuta(idRuta);

        String folio = datos.folio() == null ? "" : datos.folio().trim();

        if (folio.isEmpty()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El folio es obligatorio");
        }

        try {
            UUID.fromString(folio);
        } catch (IllegalArgumentException ex) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El folio no tiene un formato válido");
        }

        if (datos.direccion() == null || datos.direccion().isBlank()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La dirección es obligatoria");
        }

        String direccion = datos.direccion().trim();

        if (direccion.length() > 255) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La dirección no puede superar 255 caracteres");
        }

        if (datos.orden() == null || datos.orden() <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El orden debe ser mayor que cero");
        }

        if (paradaRepository.existsByFolio(folio)) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Ya existe una parada con ese folio");
        }

        if (paradaRepository.existsByRuta_IdRutaAndOrden(
                idRuta, datos.orden())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Ya existe una parada con ese orden en la ruta");
        }

        Parada parada = new Parada();
        parada.setRuta(ruta);
        parada.setFolio(folio);
        parada.setDireccion(direccion);
        parada.setOrden(datos.orden());
        parada.setEstado("PENDIENTE");

        Parada guardada;
        try {
            guardada = paradaRepository.saveAndFlush(parada);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El folio o el orden ya están asignados",
                    ex);
        }

        llamarSolicitudes(() -> solicitudesClient.marcarAsignada(folio));

        return ParadaResponse.desde(guardada);
    }

    public ParadaResponse registrarResultado(
            Long idRuta,
            Long idParada,
            ResultadoParadaRequest datos) {

        rutaService.buscarRuta(idRuta);

        Parada parada = paradaRepository
                .findByIdParadaAndRuta_IdRuta(idParada, idRuta)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "La parada no existe en esa ruta"));

        if (datos.estado() == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El estado es obligatorio");
        }

        String estado = datos.estado().trim().toUpperCase(Locale.ROOT);

        if (!Set.of("EXITOSO", "FALLIDO").contains(estado)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El resultado debe ser EXITOSO o FALLIDO");
        }

        String observacion = datos.observacion() == null
                ? null : datos.observacion().trim();

        if (observacion != null && observacion.length() > 500) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La observación no puede superar 500 caracteres");
        }

        if ("FALLIDO".equals(estado)
                && (observacion == null || observacion.isBlank())) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar el motivo del retiro fallido");
        }

        if (!"PENDIENTE".equals(parada.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La parada ya tiene un resultado registrado");
        }

        parada.setEstado(estado);
        parada.setObservacion(observacion);
        parada.setFechaResultado(LocalDateTime.now());

        Parada guardada = paradaRepository.saveAndFlush(parada);

        String resultadoSolicitud =
                "EXITOSO".equals(estado) ? "REALIZADA" : "FALLIDA";
        llamarSolicitudes(() -> solicitudesClient.registrarResultado(
                guardada.getFolio(),
                new ResultadoSolicitudRequest(resultadoSolicitud)));

        return ParadaResponse.desde(guardada);
    }

    private void llamarSolicitudes(Runnable llamada) {
        try {
            llamada.run();
        } catch (FeignException.NotFound ex) {
            throw error(HttpStatus.NOT_FOUND,
                    "El folio no existe en Solicitudes");
        } catch (FeignException.Unauthorized | FeignException.Forbidden ex) {
            throw error(HttpStatus.valueOf(ex.status()),
                    "Solicitudes rechazó el token o el rol del usuario");
        } catch (FeignException ex) {
            throw error(HttpStatus.BAD_GATEWAY,
                    "No se pudo comunicar con Solicitudes");
        }
    }

    private ResponseStatusException error(
            HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}