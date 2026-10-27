package cl.duoc.solicitudesMS.repository;

import cl.duoc.solicitudesMS.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository
        extends JpaRepository<HistorialEstado, Long> {
}