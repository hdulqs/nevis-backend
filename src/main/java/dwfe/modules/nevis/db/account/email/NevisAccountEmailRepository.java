package dwfe.modules.nevis.db.account.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NevisAccountEmailRepository extends JpaRepository<NevisAccountEmail, Long>
{
  Optional<NevisAccountEmail> findByValue(String value);
}
