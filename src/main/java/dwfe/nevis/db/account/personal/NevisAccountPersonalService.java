package dwfe.nevis.db.account.personal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NevisAccountPersonalService
{
  private final NevisAccountPersonalRepository repository;

  @Autowired
  public NevisAccountPersonalService(NevisAccountPersonalRepository repository)
  {
    this.repository = repository;
  }

  public Optional<NevisAccountPersonal> findById(Long id)
  {
    return repository.findById(id);
  }

  public Optional<NevisAccountPersonal> findByNickName(String nickName)
  {
    return repository.findByNickName(nickName);
  }

  @Transactional
  public void save(NevisAccountPersonal aPersonal)
  {
    repository.save(aPersonal);
  }
}
