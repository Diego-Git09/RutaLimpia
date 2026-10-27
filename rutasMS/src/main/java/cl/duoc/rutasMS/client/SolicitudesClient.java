package cl.duoc.rutasMS.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "solicitudes", url = "${solicitudes.url:http://localhost:8084}")
public interface SolicitudesClient {

    @PostMapping("/api/solicitudes/{folio}/asignacion")
    void marcarAsignada(@PathVariable("folio") String folio);

    @PostMapping("/api/solicitudes/{folio}/resultado")
    void registrarResultado(
            @PathVariable("folio") String folio,
            @RequestBody ResultadoSolicitudRequest cuerpo);
}