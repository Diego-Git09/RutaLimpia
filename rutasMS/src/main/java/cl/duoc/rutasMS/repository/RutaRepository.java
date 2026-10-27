package cl.duoc.rutasMS.repository;

import cl.duoc.rutasMS.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RutaRepository extends JpaRepository<Ruta, Long> {

    List<Ruta> findByIdCamionOrderByFechaAsc(Long idCamion);

    List<Ruta> findByIdConductorOrderByFechaAsc(Long idConductor);

    boolean existsByIdCamionAndFecha(Long idCamion, LocalDate fecha);

    boolean existsByIdConductorAndFecha(Long idConductor, LocalDate fecha);

    boolean existsByIdCamionAndFechaAndIdRutaNot(
            Long idCamion, LocalDate fecha, Long idRuta);

    boolean existsByIdConductorAndFechaAndIdRutaNot(
            Long idConductor, LocalDate fecha, Long idRuta);
}