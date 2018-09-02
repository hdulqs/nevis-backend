package dwfe.modules.nevis.db.account.phone;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NevisAccountPhoneRepository extends JpaRepository<NevisAccountPhone, Long>
{
  Optional<NevisAccountPhone> findByValue(String value);
}
