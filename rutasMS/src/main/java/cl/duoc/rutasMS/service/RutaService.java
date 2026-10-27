package cl.duoc.rutasMS.service;

import cl.duoc.rutasMS.model.Ruta;
import cl.duoc.rutasMS.repository.RutaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.rutasMS.repository.ParadaRepository;
import java.util.List;

@Service
@Transactional
public class RutaService {

    private final RutaRepository rutaRepository;
    private final ParadaRepository paradaRepository;

    public RutaService(
        RutaRepository rutaRepository,
        ParadaRepository paradaRepository) {
        this.rutaRepository = rutaRepository;
        this.paradaRepository = paradaRepository;
}
    @Transactional(readOnly = true)
    public List<Ruta> listarRutas() {
        return rutaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ruta buscarRuta(Long id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una ruta con ese ID"));
    }

    @Transactional(readOnly = true)
    public List<Ruta> listarPorCamion(Long idCamion) {
        validarId(idCamion, "camión");
        return rutaRepository.findByIdCamionOrderByFechaAsc(idCamion);
    }

    @Transactional(readOnly = true)
    public List<Ruta> listarPorConductor(Long idConductor) {
        validarId(idConductor, "conductor");
        return rutaRepository.findByIdConductorOrderByFechaAsc(idConductor);
    }

    public Ruta registrarRuta(Ruta ruta) {
        if (ruta.getIdRuta() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No debes enviar un ID al registrar una ruta");
        }

        validarDatos(ruta);
        validarAsignacion(ruta, null);

        return guardarRuta(ruta);
    }

    public Ruta actualizarRuta(Long id, Ruta datos) {
        Ruta actual = buscarRuta(id);

        if (datos.getIdRuta() != null
                && !datos.getIdRuta().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del cuerpo no coincide con el de la URL");
        }

        validarDatos(datos);
        validarAsignacion(datos, id);

        actual.setFecha(datos.getFecha());
        actual.setIdCamion(datos.getIdCamion());
        actual.setIdConductor(datos.getIdConductor());

        return guardarRuta(actual);
    }

    public void eliminarRuta(Long id) {
        Ruta ruta = buscarRuta(id);

        if (paradaRepository.existsByRuta_IdRuta(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes eliminar una ruta que tiene paradas");
    }

    rutaRepository.delete(ruta);
}

    private void validarDatos(Ruta ruta) {
        if (ruta.getFecha() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha es obligatoria");
        }

        validarId(ruta.getIdCamion(), "camión");
        validarId(ruta.getIdConductor(), "conductor");
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de " + campo + " debe ser mayor que cero");
        }
    }

    private void validarAsignacion(Ruta ruta, Long idActual) {
        boolean camionOcupado;
        boolean conductorOcupado;

        if (idActual == null) {
            camionOcupado = rutaRepository.existsByIdCamionAndFecha(
                    ruta.getIdCamion(), ruta.getFecha());

            conductorOcupado = rutaRepository.existsByIdConductorAndFecha(
                    ruta.getIdConductor(), ruta.getFecha());
        } else {
            camionOcupado =
                    rutaRepository.existsByIdCamionAndFechaAndIdRutaNot(
                            ruta.getIdCamion(), ruta.getFecha(), idActual);

            conductorOcupado =
                    rutaRepository.existsByIdConductorAndFechaAndIdRutaNot(
                            ruta.getIdConductor(), ruta.getFecha(), idActual);
        }

        if (camionOcupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El camión ya tiene una ruta en esa fecha");
        }

        if (conductorOcupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El conductor ya tiene una ruta en esa fecha");
        }
    }

    private Ruta guardarRuta(Ruta ruta) {
        try {
            return rutaRepository.saveAndFlush(ruta);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La asignación entra en conflicto con los datos existentes",
                    ex);
        }
    }
}