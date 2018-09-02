package dwfe.modules.nevis.db.account.personal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NevisAccountPersonalRepository extends JpaRepository<NevisAccountPersonal, Long>
{
  Optional<NevisAccountPersonal> findByNickName(String nickName);
}
