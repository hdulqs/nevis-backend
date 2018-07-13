package dwfe.nevis.db.mailing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NevisMailingRepository extends JpaRepository<NevisMailing, NevisMailing.NevisMailingId>
{
  @Query(nativeQuery = true,
          value = "SELECT * FROM nevis_mailing WHERE sent=false AND max_attempts_reached=false")
  List<NevisMailing> getNewJob();

  List<NevisMailing> findByEmail(String email);

  List<NevisMailing> findByTypeAndEmail(NevisMailingType type, String email);

  Optional<NevisMailing> findByTypeAndEmailAndData(NevisMailingType type, String email, String data);

  @Query(nativeQuery = true,
          value = "SELECT * FROM nevis_mailing WHERE type=:type AND email=:email ORDER BY created_on DESC LIMIT 1")
  Optional<NevisMailing> findLastByTypeAndEmail(@Param("type") String type, @Param("email") String email);

  Optional<NevisMailing> findByTypeAndData(NevisMailingType type, String data);

  @Query(nativeQuery = true,
          value = "SELECT * FROM nevis_mailing WHERE type=:type AND email=:email AND sent=true AND data<>''")
  List<NevisMailing> findSentNotEmptyData(@Param("type") String type, @Param("email") String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM nevis_mailing WHERE type=:type AND email=:email AND sent=true AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<NevisMailing> findLastSentNotEmptyData(@Param("type") String type, @Param("email") String email);
}
