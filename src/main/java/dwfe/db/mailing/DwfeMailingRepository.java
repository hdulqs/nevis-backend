package dwfe.db.mailing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DwfeMailingRepository extends JpaRepository<DwfeMailing, DwfeMailing.NevisMailingId>
{
  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE sent=false AND max_attempts_reached=false")
  List<DwfeMailing> getNewJob();

  List<DwfeMailing> findByEmail(String email);

  List<DwfeMailing> findByTypeAndEmail(DwfeMailingType type, String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE type=:type AND email=:email ORDER BY created_on DESC LIMIT 1")
  Optional<DwfeMailing> findLastByTypeAndEmail(@Param("type") String type, @Param("email") String email);

  Optional<DwfeMailing> findByTypeAndData(DwfeMailingType type, String data);

  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE type=:type AND email=:email AND sent=true AND data<>''")
  List<DwfeMailing> findSentNotEmptyData(@Param("type") String type, @Param("email") String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE type=:type AND email=:email AND sent=true AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<DwfeMailing> findLastSentNotEmptyData(@Param("type") String type, @Param("email") String email);
}
