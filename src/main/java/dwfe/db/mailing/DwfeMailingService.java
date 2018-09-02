package dwfe.db.mailing;

import dwfe.db.other.DwfeModule;
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

  public List<DwfeMailing> findByModuleAndEmail(DwfeModule module, String email)
  {
    return repository.findByModuleAndEmail(module, email);
  }

  public List<DwfeMailing> findByModuleAndTypeAndEmail(DwfeModule module, DwfeMailingType type, String email)
  {
    return repository.findByModuleAndTypeAndEmail(module, type, email);
  }

  public Optional<DwfeMailing> findLastByModuleAndTypeAndEmail(DwfeModule module, DwfeMailingType type, String email)
  {
    return repository.findLastByModuleAndTypeAndEmail(module.toString(), type.toString(), email);
  }

  public Optional<DwfeMailing> findByModuleAndTypeAndData(DwfeModule module, DwfeMailingType type, String data)
  {
    return repository.findByModuleAndTypeAndData(module, type, data);
  }

  public List<DwfeMailing> findSentNotEmptyData(DwfeModule module, DwfeMailingType type, String email)
  {
    return repository.findSentNotEmptyData(module.toString(), type.toString(), email);
  }

  public Optional<DwfeMailing> findLastSentNotEmptyData(DwfeModule module, DwfeMailingType type, String email)
  {
    return repository.findLastSentNotEmptyData(module.toString(), type.toString(), email);
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
