package dwfe.modules.nevis.db.account.phone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NevisAccountPhoneService
{
  private final NevisAccountPhoneRepository repository;

  @Autowired
  public NevisAccountPhoneService(NevisAccountPhoneRepository repository)
  {
    this.repository = repository;
  }

  public Optional<NevisAccountPhone> findById(Long id)
  {
    return repository.findById(id);
  }

  public Optional<NevisAccountPhone> findByValue(String value)
  {
    return repository.findByValue(value);
  }

  @Transactional
  public void save(NevisAccountPhone aPhone)
  {
    if (aPhone.getValue() != null)
      repository.save(aPhone);
  }
}
