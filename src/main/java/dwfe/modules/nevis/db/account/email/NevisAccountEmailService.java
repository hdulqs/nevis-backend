package dwfe.modules.nevis.db.account.email;

import dwfe.db.mailing.DwfeMailing;
import dwfe.db.mailing.DwfeMailingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NevisAccountEmailService
{
  private final NevisAccountEmailRepository repository;

  @Autowired
  public NevisAccountEmailService(NevisAccountEmailRepository repository)
  {
    this.repository = repository;
  }

  public Optional<NevisAccountEmail> findById(Long id)
  {
    return repository.findById(id);
  }

  public Optional<NevisAccountEmail> findByValue(String value)
  {
    return repository.findByValue(value);
  }

  @Transactional
  public void save(NevisAccountEmail aEmail)
  {
    if (aEmail.getValue() != null)
      repository.save(aEmail);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5, rollbackFor = Throwable.class)
  public void save(NevisAccountEmail aEmail,
                   DwfeMailing mailing, DwfeMailingService mailingService)
  {
    if (aEmail.getValue() != null)
      repository.save(aEmail);

    mailingService.save(mailing);
  }
}
