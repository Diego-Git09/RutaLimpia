package cl.duoc.solicitudesMS.repository;

import cl.duoc.solicitudesMS.model.SolicitudRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudRetiroRepository
        extends JpaRepository<SolicitudRetiro, Long> {

    Optional<SolicitudRetiro> findByFolio(String folio);

    List<SolicitudRetiro> findByVecinoId(String vecinoId);
}