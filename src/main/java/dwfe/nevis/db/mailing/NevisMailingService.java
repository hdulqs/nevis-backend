package dwfe.nevis.db.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NevisMailingService
{
  private final NevisMailingRepository repository;

  @Autowired
  public NevisMailingService(NevisMailingRepository repository)
  {
    this.repository = repository;
  }

  public List<NevisMailing> getNewJob()
  {
    return repository.getNewJob();
  }

  public List<NevisMailing> findByEmail(String email)
  {
    return repository.findByEmail(email);
  }

  public List<NevisMailing> findByTypeAndEmail(NevisMailingType type, String email)
  {
    return repository.findByTypeAndEmail(type, email);
  }

  public Optional<NevisMailing> findByTypeAndEmailAndData(NevisMailingType type, String email, String data)
  {
    return repository.findByTypeAndEmailAndData(type, email, data);
  }

  public Optional<NevisMailing> findLastByTypeAndEmail(NevisMailingType type, String email)
  {
    return repository.findLastByTypeAndEmail(
            String.valueOf(type.ordinal()),
            email
    );
  }

  public Optional<NevisMailing> findByTypeAndData(NevisMailingType type, String data)
  {
    return repository.findByTypeAndData(type, data);
  }

  public List<NevisMailing> findSentNotEmptyData(NevisMailingType type, String email)
  {
    return repository.findSentNotEmptyData(
            String.valueOf(type.ordinal()),
            email
    );
  }

  public Optional<NevisMailing> findLastSentNotEmptyData(NevisMailingType type, String email)
  {
    return repository.findLastSentNotEmptyData(
            String.valueOf(type.ordinal()),
            email
    );
  }

  @Transactional
  public void save(NevisMailing mailing)
  {
    if (mailing.getEmail() != null)
      repository.save(mailing);
  }

  @Transactional
  public void saveAll(List<NevisMailing> list)
  {
    repository.saveAll(list);
  }

  @Transactional
  public void deleteAll()
  {
    repository.deleteAll();
  }
}
