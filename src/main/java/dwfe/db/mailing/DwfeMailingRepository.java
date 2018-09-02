package dwfe.db.mailing;

import dwfe.db.other.DwfeModule;
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

  List<DwfeMailing> findByModuleAndEmail(DwfeModule module, String email);

  List<DwfeMailing> findByModuleAndTypeAndEmail(DwfeModule module, DwfeMailingType type, String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE module=:module AND type=:type AND email=:email ORDER BY created_on DESC LIMIT 1")
  Optional<DwfeMailing> findLastByModuleAndTypeAndEmail(@Param("module") String module, @Param("type") String type, @Param("email") String email);

  Optional<DwfeMailing> findByModuleAndTypeAndData(DwfeModule module, DwfeMailingType type, String data);

  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE module=:module AND type=:type AND email=:email AND sent=true AND data<>''")
  List<DwfeMailing> findSentNotEmptyData(@Param("module") String module, @Param("type") String type, @Param("email") String email);

  @Query(nativeQuery = true,
          value = "SELECT * FROM dwfe_mailing WHERE module=:module AND type=:type AND email=:email AND sent=true AND data<>'' ORDER BY created_on DESC LIMIT 1")
  Optional<DwfeMailing> findLastSentNotEmptyData(@Param("module") String module, @Param("type") String type, @Param("email") String email);
}
