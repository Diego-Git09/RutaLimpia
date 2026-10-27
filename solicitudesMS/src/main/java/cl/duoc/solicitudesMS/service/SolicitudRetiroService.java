package cl.duoc.solicitudesMS.service;

import cl.duoc.solicitudesMS.dto.CrearSolicitudDTO;
import cl.duoc.solicitudesMS.dto.SolicitudConsultaDTO;
import cl.duoc.solicitudesMS.dto.SolicitudCreadaDTO;
import cl.duoc.solicitudesMS.model.EstadoSolicitud;
import cl.duoc.solicitudesMS.model.HistorialEstado;
import cl.duoc.solicitudesMS.model.SolicitudRetiro;
import cl.duoc.solicitudesMS.model.TipoResiduo;
import cl.duoc.solicitudesMS.repository.HistorialEstadoRepository;
import cl.duoc.solicitudesMS.repository.SolicitudRetiroRepository;
import cl.duoc.solicitudesMS.repository.TipoResiduoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SolicitudRetiroService {
    private final HistorialEstadoRepository historialEstadoRepository;
    private final SolicitudRetiroRepository repository;
    private final TipoResiduoRepository tipoResiduoRepository;

    public SolicitudRetiroService(
            HistorialEstadoRepository historialEstadoRepository,
            SolicitudRetiroRepository repository,
            TipoResiduoRepository tipoResiduoRepository
    ) {
        this.historialEstadoRepository = historialEstadoRepository;
        this.repository = repository;
        this.tipoResiduoRepository = tipoResiduoRepository;
    }

    @Transactional
    public SolicitudCreadaDTO crearSolicitud(CrearSolicitudDTO datos) {

        SolicitudRetiro solicitud = new SolicitudRetiro();

        TipoResiduo tipoResiduo = tipoResiduoRepository
                .findById(datos.getTipoResiduoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el tipo de residuo solicitado"
                ));

        solicitud.setVecinoId(datos.getVecinoId());
        solicitud.setDireccion(datos.getDireccion());
        solicitud.setFolio(UUID.randomUUID().toString());
        solicitud.setTipoResiduo(tipoResiduo);
        solicitud.setHorarioPreferido(datos.getHorarioPreferido());
        solicitud.setCantidadKg(datos.getCantidadKg());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        SolicitudRetiro guardada = repository.save(solicitud);

        HistorialEstado historial = new HistorialEstado();
        historial.setEstado(guardada.getEstado());
        historial.setFechaHora(LocalDateTime.now());
        historial.setSolicitud(guardada);
        historialEstadoRepository.save(historial);

        return new SolicitudCreadaDTO(
                guardada.getFolio(),
                guardada.getEstado()
        );
    }

    @Transactional(readOnly = true)
    public List<SolicitudConsultaDTO> consultarSolicitudes(String vecinoId) {
        List<SolicitudRetiro> solicitudes =
                repository.findByVecinoId(vecinoId);

        List<SolicitudConsultaDTO> resultado = new ArrayList<>();

        for (SolicitudRetiro solicitud : solicitudes) {
            SolicitudConsultaDTO dto = new SolicitudConsultaDTO(
                    solicitud.getFolio(),
                    solicitud.getDireccion(),
                    solicitud.getTipoResiduo().getNombre(),
                    solicitud.getCantidadKg(),
                    solicitud.getHorarioPreferido(),
                    solicitud.getEstado()
            );
            resultado.add(dto);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<TipoResiduo> listarTiposResiduo() {
        return tipoResiduoRepository.findAll();
    }

    @Transactional
    public void registrarResultado(String folio, EstadoSolicitud resultado) {
        if (resultado != EstadoSolicitud.REALIZADA
                && resultado != EstadoSolicitud.FALLIDA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El resultado debe ser REALIZADA o FALLIDA"
            );
        }

        SolicitudRetiro solicitud = repository.findByFolio(folio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una solicitud con ese folio"
                ));

        if (solicitud.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede registrar el resultado de una solicitud ASIGNADA"
            );
        }

        solicitud.setEstado(resultado);
        SolicitudRetiro guardada = repository.save(solicitud);

        HistorialEstado historial = new HistorialEstado();
        historial.setEstado(resultado);
        historial.setFechaHora(LocalDateTime.now());
        historial.setSolicitud(guardada);

        historialEstadoRepository.save(historial);
    }

    @Transactional
    public void marcarComoAsignada(String folio) {
        SolicitudRetiro solicitud = repository.findByFolio(folio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una solicitud con ese folio"
                ));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede asignar una solicitud PENDIENTE"
            );
        }

        solicitud.setEstado(EstadoSolicitud.ASIGNADA);
        SolicitudRetiro guardada = repository.save(solicitud);

        HistorialEstado historial = new HistorialEstado();
        historial.setEstado(EstadoSolicitud.ASIGNADA);
        historial.setFechaHora(LocalDateTime.now());
        historial.setSolicitud(guardada);

        historialEstadoRepository.save(historial);
    }

}
