package dwfe.db.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DwfeMailingService
{
  private final DwfeMailingRepository repository;

  @Autowired
  public DwfeMailingService(DwfeMailingRepository repository)
  {
    this.repository = repository;
  }

  public List<DwfeMailing> getNewJob()
  {
    return repository.getNewJob();
  }

  public List<DwfeMailing> findByEmail(String email)
  {
    return repository.findByEmail(email);
  }

  public List<DwfeMailing> findByTypeAndEmail(DwfeMailingType type, String email)
  {
    return repository.findByTypeAndEmail(type, email);
  }

  public Optional<DwfeMailing> findLastByTypeAndEmail(DwfeMailingType type, String email)
  {
    return repository.findLastByTypeAndEmail(
            type.toString(), // String.valueOf(type.ordinal()),
            email
    );
  }

  public Optional<DwfeMailing> findByTypeAndData(DwfeMailingType type, String data)
  {
    return repository.findByTypeAndData(type, data);
  }

  public List<DwfeMailing> findSentNotEmptyData(DwfeMailingType type, String email)
  {
    return repository.findSentNotEmptyData(
            type.toString(), // String.valueOf(type.ordinal()),
            email
    );
  }

  public Optional<DwfeMailing> findLastSentNotEmptyData(DwfeMailingType type, String email)
  {
    return repository.findLastSentNotEmptyData(
            type.toString(), // String.valueOf(type.ordinal()),
            email
    );
  }

  @Transactional
  public void save(DwfeMailing mailing)
  {
    if (mailing.getEmail() != null)
      repository.save(mailing);
  }

  @Transactional
  public void saveAll(List<DwfeMailing> list)
  {
    repository.saveAll(list);
  }

  @Transactional
  public void deleteAll()
  {
    repository.deleteAll();
  }
}
