package dwfe.db.other.country;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NevisCountryService
{
  private final NevisCountryRepository repository;

  @Autowired
  public NevisCountryService(NevisCountryRepository repository)
  {
    this.repository = repository;
  }

  public Optional<NevisCountry> findById(String id)
  {
    return repository.findById(id);
  }
}
