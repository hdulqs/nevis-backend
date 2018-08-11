package dwfe.nevis.db.account.access;

import dwfe.nevis.db.account.email.NevisAccountEmail;
import dwfe.nevis.db.account.email.NevisAccountEmailService;
import dwfe.nevis.db.account.personal.NevisAccountPersonal;
import dwfe.nevis.db.account.personal.NevisAccountPersonalService;
import dwfe.nevis.db.account.phone.NevisAccountPhone;
import dwfe.nevis.db.account.phone.NevisAccountPhoneService;
import dwfe.nevis.db.mailing.NevisMailing;
import dwfe.nevis.db.mailing.NevisMailingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Optional;

import static dwfe.nevis.db.account.access.NevisAccountUsernameType.*;
import static dwfe.nevis.util.NevisUtil.getUsernameTypeFromStr;
import static dwfe.nevis.util.NevisUtil.isIdValid;

@Service
@Primary
@Transactional(readOnly = true)
public class NevisAccountAccessService implements UserDetailsService
{
  private final NevisAccountAccessRepository accessRepository;
  private final NevisAccountEmailService emailService;
  private final NevisAccountPhoneService phoneService;
  private final NevisAccountPersonalService personalService;

  @Autowired
  public NevisAccountAccessService(NevisAccountAccessRepository accessRepository, NevisAccountEmailService emailService, NevisAccountPhoneService phoneService, NevisAccountPersonalService personalService)
  {
    this.accessRepository = accessRepository;
    this.emailService = emailService;
    this.phoneService = phoneService;
    this.personalService = personalService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
  {
    // If you need to define a table where stored an username,
    // then, when sign-in, you need to pass an additional parameter,
    // for example, this assumes that the parameter is named 'usernameType'
    var request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    var usernameType = getUsernameTypeFromStr(request.getParameter("usernameType"));

    return findByUsername(username, usernameType).orElseThrow(() ->
            new UsernameNotFoundException(String.format("Username not exist: %s", username))
    );
  }

  public Optional<NevisAccountAccess> findByUsername(String username, NevisAccountUsernameType usernameType)
  {
    //
    // STEP 1. Get Account Id
    //
    Long id = null;
    if (username != null && !username.isEmpty())
    {
      if (usernameType == null)
      {
        // Is the Username an EMAIL?
        var accountEmailOpt = emailService.findByValue(username);
        if (accountEmailOpt.isPresent())
        {
          id = accountEmailOpt.get().getAccountId();
          usernameType = EMAIL;
        }
        else
        {
          // Is the Username an NICKNAME?
          var accountPersonalOpt = personalService.findByNickName(username);
          if (accountPersonalOpt.isPresent())
          {
            id = accountPersonalOpt.get().getAccountId();
            usernameType = NICKNAME;
          }
          else
          {
            // Is the Username a PHONE?
            var accountPhoneOpt = phoneService.findByValue(username);
            if (accountPhoneOpt.isPresent())
            {
              id = accountPhoneOpt.get().getAccountId();
              usernameType = PHONE;
            }
            else
            {
              // Is the Username an ID?
              if (isIdValid(username, new ArrayList<>()))
              {
                id = Long.parseLong(username);
                usernameType = ID;
              }
              else
              {
                // Is the Username of some OTHER type?
                // implement your logic...
                // id = ...getAccountId();
                // usernameType = ...;
              }
            }
          }
        }
      }
      else
      {
        if (EMAIL == usernameType)
        {
          var accountEmailOpt = emailService.findByValue(username);
          if (accountEmailOpt.isPresent())
            id = accountEmailOpt.get().getAccountId();
        }
        else if (NICKNAME == usernameType)
        {
          var accountPersonalOpt = personalService.findByNickName(username);
          if (accountPersonalOpt.isPresent())
            id = accountPersonalOpt.get().getAccountId();
        }
        else if (PHONE == usernameType)
        {
          var accountPhoneOpt = phoneService.findByValue(username);
          if (accountPhoneOpt.isPresent())
            id = accountPhoneOpt.get().getAccountId();
        }
        else if (ID == usernameType)
        {
          if (isIdValid(username, new ArrayList<>()))
            id = Long.parseLong(username);
        }
      }
    }

    //
    // STEP 2. Get Account Access, if Id is defined
    //
    NevisAccountAccess aAccess = null;
    if (id != null)
    {
      var aAccessOpt = accessRepository.findById(id);
      if (aAccessOpt.isPresent())
      {
        aAccess = aAccessOpt.get();
        aAccess.setUsername(username);
        aAccess.setUsernameType(usernameType);
      }
    }
    return Optional.ofNullable(aAccess);
  }

  public Optional<NevisAccountAccess> findById(Long id)
  {
    return accessRepository.findById(id);
  }

  @Transactional
  public void save(NevisAccountAccess aAccess)
  {
    accessRepository.save(aAccess);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5, rollbackFor = Throwable.class)
  public void save(NevisAccountAccess aAccess,
                   NevisMailing mailing, NevisMailingService mailingService)
  {
    accessRepository.save(aAccess);
    if (mailing.getEmail() != null)
      mailingService.save(mailing);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5, rollbackFor = Throwable.class)
  public void save(NevisAccountAccess aAccess,
                   NevisAccountEmail aEmail, NevisAccountEmailService emailService,
                   NevisAccountPhone aPhone, NevisAccountPhoneService phoneService,
                   NevisAccountPersonal aPersonal, NevisAccountPersonalService personalService,
                   NevisMailing mailing, NevisMailingService mailingService)
  {
    aAccess = accessRepository.save(aAccess);

    var id = aAccess.getId();
    aEmail.setAccountId(id);
    aPhone.setAccountId(id);
    aPersonal.setAccountId(id);

    emailService.save(aEmail);
    phoneService.save(aPhone);
    personalService.save(aPersonal);
    mailingService.save(mailing);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
  public void delete(NevisAccountAccess aAccess)
  {
    accessRepository.delete(aAccess);
  }
}
