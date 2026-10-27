package cl.duoc.flotaMS.service;

import cl.duoc.flotaMS.model.Camion;
import cl.duoc.flotaMS.repository.CamionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class CamionService {

    private static final Set<String> ESTADOS =
            Set.of("DISPONIBLE", "EN_RUTA", "EN_MANTENCION");

    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    @Transactional(readOnly = true)
    public List<Camion> listarCamiones() {
        return camionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Camion buscarCamion(Long id) {
        return camionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un camión con ese ID"));
    }

    @Transactional(readOnly = true)
    public List<Camion> listarDisponibles() {
        return camionRepository.findByEstado("DISPONIBLE");
    }

    public Camion registrarCamion(Camion camion) {
        if (camion.getIdCamion() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No debes enviar un ID al registrar un camión");
        }

        validarDatos(camion);

        if (camionRepository.existsByPatente(camion.getPatente())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un camión con esa patente");
        }

        return camionRepository.save(camion);
    }

    public Camion actualizarCamion(Long id, Camion datos) {
        Camion actual = buscarCamion(id);

        if (datos.getIdCamion() != null
                && !datos.getIdCamion().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del cuerpo no coincide con el de la URL");
        }

        validarDatos(datos);

        if (camionRepository.existsByPatenteAndIdCamionNot(
                datos.getPatente(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro camión con esa patente");
        }

        actual.setPatente(datos.getPatente());
        actual.setCapacidad(datos.getCapacidad());
        actual.setEstado(datos.getEstado());

        return camionRepository.save(actual);
    }

    public Camion cambiarEstado(Long id, String estado) {
        Camion camion = buscarCamion(id);
        camion.setEstado(validarEstado(estado));

        return camionRepository.save(camion);
    }

    public void eliminarCamion(Long id) {
        Camion camion = buscarCamion(id);

        if ("EN_RUTA".equals(camion.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes eliminar un camión que está en ruta");
        }

        camionRepository.delete(camion);
    }

    private void validarDatos(Camion camion) {
        if (camion.getPatente() == null
                || camion.getPatente().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La patente es obligatoria");
        }

        camion.setPatente(
                camion.getPatente().trim().toUpperCase(Locale.ROOT));

        if (camion.getCapacidad() == null
                || !Double.isFinite(camion.getCapacidad())
                || camion.getCapacidad() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La capacidad debe ser un número mayor que cero");
        }

        camion.setEstado(validarEstado(camion.getEstado()));
    }

    private String validarEstado(String estado) {
        if (estado == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado es obligatorio");
        }

        String normalizado = estado.trim().toUpperCase(Locale.ROOT);

        if (!ESTADOS.contains(normalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado debe ser DISPONIBLE, EN_RUTA o EN_MANTENCION");
        }

        return normalizado;
    }
}