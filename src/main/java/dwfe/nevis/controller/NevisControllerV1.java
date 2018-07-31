package dwfe.nevis.controller;

import dwfe.nevis.config.NevisConfigProperties;
import dwfe.nevis.db.account.access.NevisAccountAccess;
import dwfe.nevis.db.account.access.NevisAccountAccessService;
import dwfe.nevis.db.account.access.NevisAccountUsernameType;
import dwfe.nevis.db.account.authority.NevisAuthority;
import dwfe.nevis.db.account.email.NevisAccountEmail;
import dwfe.nevis.db.account.email.NevisAccountEmailService;
import dwfe.nevis.db.account.personal.NevisAccountPersonal;
import dwfe.nevis.db.account.personal.NevisAccountPersonalService;
import dwfe.nevis.db.account.phone.NevisAccountPhone;
import dwfe.nevis.db.account.phone.NevisAccountPhoneService;
import dwfe.nevis.db.mailing.NevisMailing;
import dwfe.nevis.db.mailing.NevisMailingService;
import dwfe.nevis.db.other.country.NevisCountryService;
import dwfe.nevis.db.other.gender.NevisGender;
import dwfe.nevis.util.NevisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static dwfe.nevis.db.account.access.NevisAccountUsernameType.*;
import static dwfe.nevis.db.mailing.NevisMailingType.*;
import static dwfe.nevis.util.NevisUtil.*;

@RestController
@RequestMapping(path = "#{nevisConfigProperties.api}", produces = "application/json; charset=utf-8")
public class NevisControllerV1
{
  private final NevisConfigProperties prop;
  private final NevisUtil util;
  private final RestTemplate restTemplate;
  private final ConsumerTokenServices tokenServices;

  private final NevisAccountAccessService accessService;
  private final NevisAccountEmailService emailService;
  private final NevisAccountPhoneService phoneService;
  private final NevisAccountPersonalService personalService;
  private final NevisMailingService mailingService;
  private final NevisCountryService countryService;

  @Autowired
  public NevisControllerV1(NevisConfigProperties prop, NevisUtil util, RestTemplateBuilder restTemplateBuilder, ConsumerTokenServices tokenServices, NevisAccountAccessService accessService, NevisAccountEmailService emailService, NevisAccountPhoneService phoneService, NevisAccountPersonalService personalService, NevisMailingService mailingService, NevisCountryService countryService)
  {
    this.prop = prop;
    this.util = util;
    this.restTemplate = restTemplateBuilder.build();
    this.tokenServices = tokenServices;

    this.accessService = accessService;
    this.emailService = emailService;
    this.phoneService = phoneService;
    this.personalService = personalService;
    this.mailingService = mailingService;
    this.countryService = countryService;
  }


  //-------------------------------------------------------
  // Auth
  //

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("#{nevisConfigProperties.resource.signOut}")
  public String signOut(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();

    var accessToken = ((AuthorizationServerTokenServices) tokenServices).getAccessToken(authentication);
    tokenServices.revokeToken(accessToken.getValue());

    return getResponse(errorCodes);
  }


  //-------------------------------------------------------
  // Account.Common
  //

  @PostMapping("#{nevisConfigProperties.resource.canUseUsername}")
  public String canUseUsername(@RequestBody ReqUsername req)
  {
    var errorCodes = new ArrayList<String>();
    var username = req.username;
    var usernameType = req.usernameType;

    if (isDefaultPreCheckOk(username, "username", errorCodes))
      if (NICKNAME == usernameType)
        canUseNickName(username, errorCodes);
      else if (PHONE == usernameType)
        canUsePhone(username, errorCodes);
      else if (ID == usernameType)
        canUseId(username, errorCodes);
      else
        // if usernameType was not passed,
        // then it is considered to be equal to EMAIL
        canUseEmail(username, errorCodes);

    return getResponse(errorCodes);
  }

  @PostMapping("#{nevisConfigProperties.resource.canUsePassword}")
  public String canUsePassword(@RequestBody ReqPassword req)
  {
    var errorCodes = new ArrayList<String>();
    canUsePassword(req.password, "password", errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{nevisConfigProperties.resource.googleCaptchaValidate}")
  public String googleCaptchaValidate(@RequestBody ReqGoogleCaptchaResponse req)
  {
    var errorCodes = new ArrayList<String>();

    if (isDefaultPreCheckOk(req.googleResponse, "google-response", errorCodes))
    {
      var url = String.format(prop.getGoogleCaptcha().getSiteVerifyUrlTemplate(),
              prop.getGoogleCaptcha().getSecretKey(), req.googleResponse);

      FutureTask<ResponseEntity<String>> exchange =
              new FutureTask<>(() -> restTemplate.exchange(url, HttpMethod.POST, null, String.class));
      new Thread(exchange).start();

      try
      {
        var response = exchange.get(7, TimeUnit.SECONDS);
        if (response.getStatusCodeValue() == 200)
        {
          var success = (Boolean) getPropValueFromJson("success", response.getBody());
          if (!success)
            errorCodes.add("google-captcha-detected-robot");
        }
        else
          errorCodes.add("error-google-captcha-gateway");
      }
      catch (Throwable e)
      {
        errorCodes.add("timeout-google-captcha-gateway");
      }
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{nevisConfigProperties.resource.createAccount}")
  public String createAccount(@RequestBody ReqCreateAccount req)
  {
    var errorCodes = new ArrayList<String>();

    //
    // Username
    //
    if (req.email == null
            && req.nickName == null
            && req.phone == null)
      errorCodes.add("username-must-be-passed");

    //
    // Password
    //
    var password = req.password;
    var automaticallyGeneratedPassword = "";
    if (password == null) // if password wasn't passed
    {
      if (req.email == null)
        errorCodes.add("the-generated-password-should-be-sent-somewhere");
        //   - to email?
        //   - to phone? not implemented
      else
      {
        automaticallyGeneratedPassword = getRandomStrAlphaDigit(15);
        password = automaticallyGeneratedPassword;
      }
    }

    //
    // Create Account
    //
    if (errorCodes.size() == 0
            && canUsePassword(password, "password", errorCodes)
            && canUseGender(req.gender, errorCodes)
            && canUseDateOfBirth(req.dateOfBirth, errorCodes)
            && canUseCountry(req.country, errorCodes)    // query to database on last check
            && canUseEmail(req.email, errorCodes)        // query to database on last check
            && canUseNickName(req.nickName, errorCodes)  // query to database on last check
            && canUsePhone(req.phone, errorCodes))       // query to database on last check
    {
      // TABLE - Access
      var aAccess = new NevisAccountAccess();
      aAccess.setPassword(preparePasswordForDB(password));
      aAccess.setAuthorities(Set.of(NevisAuthority.of("USER")));
      aAccess.setAccountNonExpired(true);
      aAccess.setCredentialsNonExpired(true);
      aAccess.setAccountNonLocked(true);
      aAccess.setEnabled(true);

      // TABLE - Email
      var aEmail = new NevisAccountEmail();
      aEmail.setValue(req.email);
      aEmail.setNonPublic(true);
      aEmail.setConfirmed(req.email != null && !automaticallyGeneratedPassword.isEmpty());

      // TABLE - Phone
      var aPhone = new NevisAccountPhone();
      aPhone.setValue(req.phone);
      aPhone.setNonPublic(true);
      aPhone.setConfirmed(false);

      // TABLE - Personal
      var aPersonal = new NevisAccountPersonal();

      aPersonal.setNickName(req.nickName);
      aPersonal.setNickNameNonPublic(true);

      aPersonal.setFirstName(req.firstName);
      aPersonal.setFirstNameNonPublic(true);

      aPersonal.setMiddleName(req.middleName);
      aPersonal.setMiddleNameNonPublic(true);

      aPersonal.setLastName(req.lastName);
      aPersonal.setLastNameNonPublic(true);

      aPersonal.setGender(reqPrepareGender(req.gender));
      aPersonal.setGenderNonPublic(true);

      aPersonal.setDateOfBirth(reqPrepareDateOfBirth(req.dateOfBirth));
      aPersonal.setDateOfBirthNonPublic(true);

      aPersonal.setCountry(req.country);
      aPersonal.setCountryNonPublic(true);

      aPersonal.setCity(req.city);
      aPersonal.setCityNonPublic(true);

      aPersonal.setCompany(req.company);
      aPersonal.setCompanyNonPublic(true);

      aPersonal.setPositionHeld(req.positionHeld);
      aPersonal.setPositionHeldNonPublic(true);

      // TABLE - Mailing
      var mailingType = automaticallyGeneratedPassword.isEmpty() ? WELCOME_ONLY : WELCOME_PASSWORD;
      var mailing = NevisMailing.of(mailingType, aEmail.getValue(), automaticallyGeneratedPassword);

      // Save new Account
      accessService.save(
              aAccess,
              aEmail, emailService,
              aPhone, phoneService,
              aPersonal, personalService,
              mailing, mailingService
      );
    }
    return getResponse(errorCodes);
  }

  @GetMapping("#{nevisConfigProperties.resource.id}")
  public String id(@PathVariable("id") Long id)
  {
    var errorCodes = new ArrayList<String>();
    var list = new ArrayList<String>();
    var data = "";

    var aAccessOpt = accessService.findById(id);
    if (aAccessOpt.isPresent())
    {
      var aAccess = aAccessOpt.get();
      list.add("\"access\":" + respPrepareAccountAccess(aAccess, true));

      var aEmailOpt = emailService.findById(id);
      list.add("\"email\":" + aEmailOpt.map(aEmail -> respPrepareAccountEmail(aEmail, true)).orElse("{}"));

      var aPhoneOpt = phoneService.findById(id);
      list.add("\"phone\":" + aPhoneOpt.map(aPhone -> respPrepareAccountPhone(aPhone, true)).orElse("{}"));

      var aPersonalOpt = personalService.findById(id);
      list.add("\"personal\":" + aPersonalOpt.map(aPersonal -> respPrepareAccountPersonal(aPersonal, true)).orElse("{}"));

      data = listToJson(list);
    }
    else errorCodes.add("id-not-exist");

    return getResponse(errorCodes, data);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.deleteAccount}")
  public String deleteAccount(@RequestBody ReqDeleteAccount req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var curpass = req.curpass;

    if (isDefaultPreCheckOk(curpass, req.curpassFieldName, errorCodes))
    {
      var aAccess = accessService.findById(id).get();
      if (matchPassword(curpass, aAccess.getPassword()))
        accessService.delete(aAccess);
      else errorCodes.add("wrong-" + req.curpassFieldName);
    }
    return getResponse(errorCodes);
  }


  //-------------------------------------------------------
  // Account.Access
  //

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("#{nevisConfigProperties.resource.getAccountAccess}")
  public String getAccountAccess(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var data = respPrepareAccountAccess(getAccessFromAuth(authentication), false);
    return getResponse(errorCodes, data);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.passwordChange}")
  public String passwordChange(@RequestBody ReqPasswordChange req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var curpass = req.curpass;
    var newpass = req.newpass;

    if (isDefaultPreCheckOk(curpass, req.curpassFieldName, errorCodes)
            && canUsePassword(newpass, req.newpassFieldName, errorCodes))
    {
      var aAccess = accessService.findById(id).get();
      if (matchPassword(curpass, aAccess.getPassword()))
      {
        var email = emailService.findById(id).map(NevisAccountEmail::getValue).orElse(null);
        aAccess.setPassword(preparePasswordForDB(newpass));
        accessService.save(aAccess, NevisMailing.of(PASSWORD_WAS_CHANGED, email), mailingService);
      }
      else errorCodes.add("wrong-" + req.curpassFieldName);
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{nevisConfigProperties.resource.passwordResetReq}")
  public String passwordResetReq(@RequestBody ReqEmail req)
  {
    var errorCodes = new ArrayList<String>();
    var email = req.email;
    var type = PASSWORD_RESET_CONFIRM;

    if (isDefaultPreCheckOk(email, "email", errorCodes)
            && standardEmailCheck(email, "email", errorCodes)
            && util.isAllowedNewRequestForMailing(type, email, errorCodes))
    {
      var aEmailOpt = emailService.findByValue(email);
      if (aEmailOpt.isPresent())
      {
        mailingService.save(NevisMailing.of(type, email, getRandomStrAlphaDigit(40)));
      }
      else errorCodes.add("email-not-exist");
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{nevisConfigProperties.resource.passwordReset}")
  public String passwordReset(@RequestBody ReqPasswordReset req)
  {
    var errorCodes = new ArrayList<String>();
    var key = req.key;
    var newpass = req.newpass;
    var data = new HashMap<String, Object>();

    if (canUsePassword(newpass, req.newpassField, errorCodes)
            && isDefaultPreCheckOk(key, req.keyFieldFullName, errorCodes))
    {
      var mailingOpt = mailingService.findByTypeAndData(PASSWORD_RESET_CONFIRM, key);
      if (mailingOpt.isPresent())
      {
        var mailing = mailingOpt.get();
        var email = mailing.getEmail();
        data.put("username", email);

        var aEmailOpt = emailService.findByValue(email);
        if (aEmailOpt.isPresent())
        {
          var id = aEmailOpt.get().getAccountId();

          // the AccountAccess is guaranteed to exist because:
          // CONSTRAINT nevis_account_email_account_id_fk FOREIGN KEY (account_id) REFERENCES nevis_account_access (id)
          var aAccess = accessService.findById(id).get();
          aAccess.setPassword(preparePasswordForDB(newpass));
          mailing.clear();

          accessService.save(
                  aAccess,
                  mailing, mailingService
          );
        }
        else errorCodes.add("email-not-exist");
      }
      else errorCodes.add(req.keyFieldFullName + "-not-exist");
    }
    return getResponse(errorCodes, data);
  }


  //-------------------------------------------------------
  // Account.Email
  //

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("#{nevisConfigProperties.resource.getAccountEmail}")
  public String getAccountEmail(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var data = "";

    var aEmailOpt = emailService.findById(id);
    if (aEmailOpt.isPresent())
      data = respPrepareAccountEmail(aEmailOpt.get(), false);
    else
      errorCodes.add("account-is-not-linked-to-email");
    return getResponse(errorCodes, data);
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("#{nevisConfigProperties.resource.emailConfirmReq}")
  public String emailConfirmReq(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var type = EMAIL_CONFIRM;

    var aEmailOpt = emailService.findById(id);
    if (aEmailOpt.isPresent())
    {
      var aEmail = emailService.findById(id).get();
      var email = aEmail.getValue();
      if (aEmail.isConfirmed())
        errorCodes.add("email-is-already-confirmed");
      else if (util.isAllowedNewRequestForMailing(type, email, errorCodes))
        mailingService.save(NevisMailing.of(type, email, getRandomStrAlphaDigit(40)));
    }
    else errorCodes.add("no-email-associated-with-account");
    return getResponse(errorCodes);
  }

  @PostMapping("#{nevisConfigProperties.resource.emailConfirm}")
  public String emailConfirm(@RequestBody ReqConfirm req)
  {
    var errorCodes = new ArrayList<String>();
    var key = req.key;
    var fieldName = "confirm-key";

    if (isDefaultPreCheckOk(key, fieldName, errorCodes))
    {
      var mailingOpt = mailingService.findByTypeAndData(EMAIL_CONFIRM, key);
      if (mailingOpt.isPresent())
      {
        var mailing = mailingOpt.get();
        var aEmailOpt = emailService.findByValue(mailing.getEmail());
        if (aEmailOpt.isPresent())
        {
          var aEmail = aEmailOpt.get();
          aEmail.setConfirmed(true); // email now confirmed
          mailing.clear();
          emailService.save(aEmail, mailing, mailingService);
        }
        else errorCodes.add("linked-email-not-exist");
      }
      else errorCodes.add(fieldName + "-not-exist");
    }
    return getResponse(errorCodes);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.emailChange}")
  public String emailChange(@RequestBody ReqEmailChange req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var newemail = req.newemail;
    var curpass = req.curpass;

    if (isDefaultPreCheckOk(curpass, req.curpassFieldName, errorCodes)
            && isDefaultPreCheckOk(newemail, req.newemailFieldName, errorCodes)
            && canUseEmail(newemail, errorCodes))
    {
      var aAccess = accessService.findById(id).get();
      if (matchPassword(curpass, aAccess.getPassword()))
      {
        var aEmail = new NevisAccountEmail();
        String curemail = null;

        var aEmailOpt = emailService.findById(id);
        if (aEmailOpt.isPresent())
        {
          aEmail = aEmailOpt.get();
          curemail = aEmail.getValue();
        }

        if (curemail == null)
        {
          aEmail.setAccountId(id);
          aEmail.setNonPublic(true);
        }
        aEmail.setValue(newemail);
        aEmail.setConfirmed(false);
        emailService.save(aEmail);
      }
      else errorCodes.add("wrong-" + req.curpassFieldName);
    }
    return getResponse(errorCodes);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.updateAccountEmail}")
  public String updateAccountEmail(@RequestBody ReqUpdateAccountEmail req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);

    var aEmailOpt = emailService.findById(id);
    if (aEmailOpt.isPresent())
    {
      var aEmail = aEmailOpt.get();
      var isModified = false;

      var newNonPublic = req.nonPublic;
      if (newNonPublic != null && !newNonPublic.equals(aEmail.isNonPublic()))
      {
        aEmail.setNonPublic(newNonPublic);
        isModified = true;
      }

      if (isModified)
        emailService.save(aEmail);
    }
    else errorCodes.add("no-email-associated-with-account");
    return getResponse(errorCodes);
  }


  //-------------------------------------------------------
  // Account.Phone
  //

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("#{nevisConfigProperties.resource.getAccountPhone}")
  public String getAccountPhone(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var data = "";

    var aPhoneOpt = phoneService.findById(id);
    if (aPhoneOpt.isPresent())
      data = respPrepareAccountPhone(aPhoneOpt.get(), false);
    else
      errorCodes.add("account-is-not-linked-to-phone");
    return getResponse(errorCodes, data);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.phoneChange}")
  public String phoneChange(@RequestBody ReqPhoneChange req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var newphone = req.newphone;
    var curpass = req.curpass;

    if (isDefaultPreCheckOk(curpass, req.curpassFieldName, errorCodes)
            && isDefaultPreCheckOk(newphone, req.newphoneFieldName, errorCodes)
            && canUsePhone(newphone, errorCodes))
    {
      var aAccess = accessService.findById(id).get();
      if (matchPassword(curpass, aAccess.getPassword()))
      {
        var aPhone = new NevisAccountPhone();
        String curphone = null;

        var aPhoneOpt = phoneService.findById(id);
        if (aPhoneOpt.isPresent())
        {
          aPhone = aPhoneOpt.get();
          curphone = aPhone.getValue();
        }

        if (curphone == null)
        {
          aPhone.setAccountId(id);
          aPhone.setNonPublic(true);
        }
        aPhone.setValue(newphone);
        aPhone.setConfirmed(false);
        phoneService.save(aPhone);
      }
      else errorCodes.add("wrong-" + req.curpassFieldName);
    }
    return getResponse(errorCodes);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.updateAccountPhone}")
  public String updateAccountPhone(@RequestBody ReqUpdateAccountPhone req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);

    var aPhoneOpt = phoneService.findById(id);
    if (aPhoneOpt.isPresent())
    {
      var aPhone = aPhoneOpt.get();
      var isModified = false;

      var newNonPublic = req.nonPublic;
      if (newNonPublic != null && !newNonPublic.equals(aPhone.isNonPublic()))
      {
        aPhone.setNonPublic(newNonPublic);
        isModified = true;
      }

      if (isModified)
        phoneService.save(aPhone);
    }
    else errorCodes.add("no-phone-associated-with-account");
    return getResponse(errorCodes);
  }


  //-------------------------------------------------------
  // Account.Personal
  //

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("#{nevisConfigProperties.resource.getAccountPersonal}")
  public String getAccountPersonal(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var data = "";

    var aPersonalOpt = personalService.findById(id);
    if (aPersonalOpt.isPresent())
      data = respPrepareAccountPersonal(aPersonalOpt.get(), false);
    return getResponse(errorCodes, data);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.nicknameChange}")
  public String nicknameChange(@RequestBody ReqNickNameChange req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var newNickName = req.newNickName;
    var curpass = req.curpass;

    if (isDefaultPreCheckOk(curpass, req.curpassFieldName, errorCodes)
            && isDefaultPreCheckOk(newNickName, req.newNickNameFieldName, errorCodes)
            && canUseNickName(newNickName, errorCodes))
    {
      var aAccess = accessService.findById(id).get();
      if (matchPassword(curpass, aAccess.getPassword()))
      {
        var aPersonal = personalService.findById(id).get();
        aPersonal.setNickName(newNickName);
        personalService.save(aPersonal);
      }
      else errorCodes.add("wrong-" + req.curpassFieldName);
    }
    return getResponse(errorCodes);
  }

  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("#{nevisConfigProperties.resource.updateAccountPersonal}")
  public String updateAccountPersonal(@RequestBody Map<String, Object> req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var data = "";

    var aPersonalOpt = personalService.findById(id);
    if (aPersonalOpt.isPresent())
    {
      var aPersonal = aPersonalOpt.get();
      var isModified = false;

      if (req.containsKey("nickNameNonPublic"))
      {
        var newNickNameNonPublic = (Boolean) req.get("nickNameNonPublic");
        if (isDifferentValues(newNickNameNonPublic, aPersonal.getNickNameNonPublic()))
        {
          aPersonal.setNickNameNonPublic(newNickNameNonPublic);
          isModified = true;
        }
      }

      if (req.containsKey("firstName"))
      {
        var newFirstName = (String) req.get("firstName");
        if (!isObjEquals(newFirstName, aPersonal.getFirstName()))
        {
          aPersonal.setFirstName(newFirstName);
          isModified = true;
        }
      }

      if (req.containsKey("firstNameNonPublic"))
      {
        var newFirstNameNonPublic = (Boolean) req.get("firstNameNonPublic");
        if (isDifferentValues(newFirstNameNonPublic, aPersonal.getFirstNameNonPublic()))
        {
          aPersonal.setFirstNameNonPublic(newFirstNameNonPublic);
          isModified = true;
        }
      }

      if (req.containsKey("middleName"))
      {
        var newMiddleName = (String) req.get("middleName");
        if (!isObjEquals(newMiddleName, aPersonal.getMiddleName()))
        {
          aPersonal.setMiddleName(newMiddleName);
          isModified = true;
        }
      }

      if (req.containsKey("middleNameNonPublic"))
      {
        var newMiddleNameNonPublic = (Boolean) req.get("middleNameNonPublic");
        if (isDifferentValues(newMiddleNameNonPublic, aPersonal.getMiddleNameNonPublic()))
        {
          aPersonal.setMiddleNameNonPublic(newMiddleNameNonPublic);
          isModified = true;
        }
      }

      if (req.containsKey("lastName"))
      {
        var newLastName = (String) req.get("lastName");
        if (!isObjEquals(newLastName, aPersonal.getLastName()))
        {
          aPersonal.setLastName(newLastName);
          isModified = true;
        }
      }

      if (req.containsKey("lastNameNonPublic"))
      {
        var newLastNameNonPublic = (Boolean) req.get("lastNameNonPublic");
        if (isDifferentValues(newLastNameNonPublic, aPersonal.getLastNameNonPublic()))
        {
          aPersonal.setLastNameNonPublic(newLastNameNonPublic);
          isModified = true;
        }
      }

      if (req.containsKey("gender"))
      {
        var newGenderStr = (String) req.get("gender");
        if (canUseGender(newGenderStr, errorCodes))
        {
          var newGender = reqPrepareGender(newGenderStr);
          if (!isObjEquals(newGender, aPersonal.getGender()))
          {
            aPersonal.setGender(newGender);
            isModified = true;
          }
        }
      }

      if (req.containsKey("genderNonPublic"))
      {
        var newGenderNonPublic = (Boolean) req.get("genderNonPublic");
        if (isDifferentValues(newGenderNonPublic, aPersonal.getGenderNonPublic()))
        {
          aPersonal.setGenderNonPublic(newGenderNonPublic);
          isModified = true;
        }
      }

      if (req.containsKey("dateOfBirth"))
      {
        var newDateOfBirthStr = (String) req.get("dateOfBirth");
        if (canUseDateOfBirth(newDateOfBirthStr, errorCodes))
        {
          var newDateOfBirth = reqPrepareDateOfBirth(newDateOfBirthStr);
          if (!isObjEquals(newDateOfBirth, aPersonal.getDateOfBirth()))
          {
            aPersonal.setDateOfBirth(newDateOfBirth);
            isModified = true;
          }
        }
      }

      if (req.containsKey("dateOfBirthNonPublic"))
      {
        var newDateOfBirthNonPublic = (Boolean) req.get("dateOfBirthNonPublic");
        if (isDifferentValues(newDateOfBirthNonPublic, aPersonal.getDateOfBirthNonPublic()))
        {
          aPersonal.setDateOfBirthNonPublic(newDateOfBirthNonPublic);
          isModified = true;
        }
      }

      if (errorCodes.size() == 0 && req.containsKey("country"))
      {
        var newCountry = (String) req.get("country");
        if (canUseCountry(newCountry, errorCodes)
                && !isObjEquals(newCountry, aPersonal.getCountry()))
        {
          aPersonal.setCountry(newCountry);
          isModified = true;
        }
      }

      if (req.containsKey("countryNonPublic"))
      {
        var newCountryNonPublic = (Boolean) req.get("countryNonPublic");
        if (isDifferentValues(newCountryNonPublic, aPersonal.getCountryNonPublic()))
        {
          aPersonal.setCountryNonPublic(newCountryNonPublic);
          isModified = true;
        }
      }

      if (req.containsKey("city"))
      {
        var newCity = (String) req.get("city");
        if (!isObjEquals(newCity, aPersonal.getCity()))
        {
          aPersonal.setCity(newCity);
          isModified = true;
        }
      }

      var newCityNonPublic = req.cityNonPublic;
      if (isDifferentValues(newCityNonPublic, aPersonal.getCityNonPublic()))
      {
        aPersonal.setCityNonPublic(newCityNonPublic);
        isModified = true;
      }

      var newCompany = req.company;
      if (isDifferentValues(newCompany, aPersonal.getCompany()))
      {
        aPersonal.setCompany(newCompany);
        isModified = true;
      }

      var newCompanyNonPublic = req.companyNonPublic;
      if (isDifferentValues(newCompanyNonPublic, aPersonal.getCompanyNonPublic()))
      {
        aPersonal.setCompanyNonPublic(newCompanyNonPublic);
        isModified = true;
      }

      var newPositionHeld = req.positionHeld;
      if (isDifferentValues(newPositionHeld, aPersonal.getPositionHeld()))
      {
        aPersonal.setPositionHeld(newPositionHeld);
        isModified = true;
      }

      var newPositionHeldNonPublic = req.positionHeldNonPublic;
      if (isDifferentValues(newPositionHeldNonPublic, aPersonal.getPositionHeldNonPublic()))
      {
        aPersonal.setPositionHeldNonPublic(newPositionHeldNonPublic);
        isModified = true;
      }

      if (isModified && errorCodes.size() == 0)
        personalService.save(aPersonal);

      data = respPrepareAccountPersonal(aPersonal, false);
    }
    return getResponse(errorCodes, data);
  }


  //-------------------------------------------------------
  //  UTILs
  //

  private static NevisAccountAccess getAccessFromAuth(OAuth2Authentication authentication)
  {
    return ((NevisAccountAccess) authentication.getPrincipal());
  }

  private static Long getId(OAuth2Authentication authentication)
  {
    return getAccessFromAuth(authentication).getId();
  }

  private boolean canUseId(String id, List<String> errorCodes)
  {
    if (isDefaultPreCheckOk(id, "id", errorCodes)
            && isIdValid(id, errorCodes)
            && accessService.findById(Long.parseLong(id)).isPresent())
    {
      errorCodes.add("id-present-in-database");
    }
    return errorCodes.size() == 0;
  }

  private boolean canUseNickName(String nickName, List<String> errorCodes)
  {
    if (nickName == null)
      return true;
    else if (isEmptyPreCheckOk(nickName, "nickName", errorCodes)
            && isExceededMaxCheckOk(nickName, "nickName", 100, errorCodes)
            && personalService.findByNickName(nickName).isPresent())
    {
      errorCodes.add("nickName-present-in-database");
    }
    return errorCodes.size() == 0;
  }

  private boolean canUseEmail(String email, List<String> errorCodes)
  {
    if (email == null)
      return true;
    else if (isEmptyPreCheckOk(email, "email", errorCodes)
            && standardEmailCheck(email, "email", errorCodes)
            && emailService.findByValue(email).isPresent())
    {
      errorCodes.add("email-present-in-database");
    }
    return errorCodes.size() == 0;
  }

  private boolean canUsePhone(String phone, List<String> errorCodes)
  {
    if (phone == null)
      return true;
    else if (isEmptyPreCheckOk(phone, "phone", errorCodes)
            && isExceededMaxCheckOk(phone, "phone", 100, errorCodes)
            && phoneService.findByValue(phone).isPresent())
    {
      errorCodes.add("phone-present-in-database");
    }
    return errorCodes.size() == 0;
  }

  private boolean canUsePassword(String password, String name, List<String> errorCodes)
  {
    if (isDefaultPreCheckOk(password, name, errorCodes))
      if (isStringBcrypted(password))
        return true;
      else
        return isRangeMinMaxCheckOk(password, name, 6, 55, errorCodes);
    return errorCodes.size() == 0;
  }

  private boolean canUseGender(String gender, List<String> errorCodes)
  {
    if (gender == null)
      return true;
    else if (isEmptyPreCheckOk(gender, "gender", errorCodes))
    {
      final var genderUpperCased = gender.toUpperCase();
      if (Arrays.stream(NevisGender.values()).noneMatch((t) -> t.name().equals(genderUpperCased)))
        errorCodes.add("invalid-gender");
    }
    return errorCodes.size() == 0;
  }

  private boolean canUseDateOfBirth(String dateOfBirth, List<String> errorCodes)
  {
    if (dateOfBirth == null)
      return true;
    else if (isEmptyPreCheckOk(dateOfBirth, "dateOfBirth", errorCodes))
      try
      {
        LocalDate.parse(dateOfBirth);
      }
      catch (DateTimeParseException e)
      {
        errorCodes.add("dateOfBirth-cannot-be-parsed");
      }
    return errorCodes.size() == 0;
  }

  private boolean canUseCountry(String country, List<String> errorCodes)
  {
    if (country == null)
      return true;
    else if (isEmptyPreCheckOk(country, "country", errorCodes)
            && !countryService.findById(country.toUpperCase()).isPresent())
    {
      errorCodes.add("invalid-country");
    }
    return errorCodes.size() == 0;
  }

  private static boolean isDifferentValues(Object newValue, Object oldValue)
  {
    return newValue != null && !newValue.equals(oldValue);
  }

  private static boolean isObjEquals(Object o1, Object o2)
  {
    if (o1 == null && o2 == null) // (null,null)
    {
      return true;
    }
    else if (o1 == null || o2 == null) // (null,obj)  OR  (obj,null)
    {
      return false;
    }
    else // (obj,obj)
    {
      return o1.equals(o2);
    }
  }

  private static NevisGender reqPrepareGender(String gender)
  {
    return gender == null ? null : NevisGender.valueOf(gender.toUpperCase());
  }

  private static LocalDate reqPrepareDateOfBirth(String dateOfBirth)
  {
    return dateOfBirth == null ? null : LocalDate.parse(dateOfBirth);
  }


  //-------------------------------------------------------
  // Response
  //

  private static String getResponse(List<String> errorCodes)
  {
    if (errorCodes.size() == 0)
      return "{\"success\": true}";
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  private static String getResponse(List<String> errorCodes, String data)
  {
    if (errorCodes.size() == 0)
      return getResponseSuccessWithData(data);
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  private static String getResponse(List<String> errorCodes, Map<String, Object> data)
  {
    if (errorCodes.size() == 0)
      return getResponseSuccessWithData(getJsonFromObj(data));
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  private static String getResponseSuccessWithData(String data)
  {
    return String.format("{\"success\": true, \"data\": %s}", data);
  }

  private static String getResponseWithErrorCodes(List<String> errorCodes)
  {
    return String.format("{\"success\": false, \"error-codes\": %s}", getJsonFromObj(errorCodes));
  }

  private static String nullableValueToResp(String field, Object value)
  {
    return value == null
            ? "\"" + field + "\":null"
            : "\"" + field + "\":\"" + value + "\"";
  }

  private static String respPrepareAccountAccess(NevisAccountAccess aAccess, boolean onPublic)
  {
    var list = new ArrayList<String>();

    var id = "\"id\":" + aAccess.getId();

    list.add(id);

    if (!onPublic)
    {
      list.add("\"authorities\":" + getAuthorities(aAccess.getAuthorities(), true));
      list.add("\"accountNonExpired\":" + aAccess.isAccountNonExpired());
      list.add("\"credentialsNonExpired\":" + aAccess.isCredentialsNonExpired());
      list.add("\"accountNonLocked\":" + aAccess.isAccountNonLocked());
      list.add("\"enabled\":" + aAccess.isEnabled());
      list.add("\"createdOn\":\"" + formatDateTimeToUTCstring(aAccess.getCreatedOn()) + "\"");
      list.add("\"updatedOn\":\"" + formatDateTimeToUTCstring(aAccess.getUpdatedOn()) + "\"");
    }
    return listToJson(list);
  }

  private static String respPrepareAccountEmail(NevisAccountEmail aEmail, boolean onPublic)
  {
    var list = new ArrayList<String>();

    var value = "\"value\":\"" + aEmail.getValue() + "\"";

    if (onPublic)
    {
      if (!aEmail.isNonPublic())
        list.add(value);
    }
    else
    {
      list.add(value);
      list.add("\"nonPublic\":" + aEmail.isNonPublic());
      list.add("\"confirmed\":" + aEmail.isConfirmed());
      list.add("\"updatedOn\":\"" + formatDateTimeToUTCstring(aEmail.getUpdatedOn()) + "\"");
    }
    return listToJson(list);
  }

  private static String respPrepareAccountPhone(NevisAccountPhone aPhone, boolean onPublic)
  {
    var list = new ArrayList<String>();

    var value = "\"value\":\"" + aPhone.getValue() + "\"";

    if (onPublic)
    {
      if (!aPhone.isNonPublic())
        list.add(value);
    }
    else
    {
      list.add(value);
      list.add("\"nonPublic\":" + aPhone.isNonPublic());
      list.add("\"confirmed\":" + aPhone.isConfirmed());
      list.add("\"updatedOn\":" + "\"" + formatDateTimeToUTCstring(aPhone.getUpdatedOn()) + "\"" + "");
    }
    return listToJson(list);
  }

  private static String respPrepareAccountPersonal(NevisAccountPersonal aPersonal, boolean onPublic)
  {
    var list = new ArrayList<String>();

    var nickName = nullableValueToResp("nickName", aPersonal.getNickName());
    var firstName = nullableValueToResp("firstName", aPersonal.getFirstName());
    var middleName = nullableValueToResp("middleName", aPersonal.getMiddleName());
    var lastName = nullableValueToResp("lastName", aPersonal.getLastName());
    var gender = nullableValueToResp("gender", aPersonal.getGender());
    var dateOfBirth = nullableValueToResp("dateOfBirth", aPersonal.getDateOfBirth());
    var country = nullableValueToResp("country", aPersonal.getCountry());
    var city = nullableValueToResp("city", aPersonal.getCity());
    var company = nullableValueToResp("company", aPersonal.getCompany());
    var positionHeld = nullableValueToResp("positionHeld", aPersonal.getPositionHeld());

    if (onPublic)
    {
      if (!aPersonal.getNickNameNonPublic())
        list.add(nickName);
      if (!aPersonal.getFirstNameNonPublic())
        list.add(firstName);
      if (!aPersonal.getMiddleNameNonPublic())
        list.add(middleName);
      if (!aPersonal.getLastNameNonPublic())
        list.add(lastName);
      if (!aPersonal.getGenderNonPublic())
        list.add(gender);
      if (!aPersonal.getDateOfBirthNonPublic())
        list.add(dateOfBirth);
      if (!aPersonal.getCountryNonPublic())
        list.add(country);
      if (!aPersonal.getCityNonPublic())
        list.add(city);
      if (!aPersonal.getCompanyNonPublic())
        list.add(company);
      if (!aPersonal.getPositionHeldNonPublic())
        list.add(positionHeld);
    }
    else
    {
      list.add(nickName);
      list.add("\"nickNameNonPublic\":" + aPersonal.getNickNameNonPublic());
      list.add(firstName);
      list.add("\"firstNameNonPublic\":" + aPersonal.getFirstNameNonPublic());
      list.add(middleName);
      list.add("\"middleNameNonPublic\":" + aPersonal.getMiddleNameNonPublic());
      list.add(lastName);
      list.add("\"lastNameNonPublic\":" + aPersonal.getLastNameNonPublic());
      list.add(gender);
      list.add("\"genderNonPublic\":" + aPersonal.getGenderNonPublic());
      list.add(dateOfBirth);
      list.add("\"dateOfBirthNonPublic\":" + aPersonal.getDateOfBirthNonPublic());
      list.add(country);
      list.add("\"countryNonPublic\":" + aPersonal.getCountryNonPublic());
      list.add(city);
      list.add("\"cityNonPublic\":" + aPersonal.getCityNonPublic());
      list.add(company);
      list.add("\"companyNonPublic\":" + aPersonal.getCompanyNonPublic());
      list.add(positionHeld);
      list.add("\"positionHeldNonPublic\":" + aPersonal.getPositionHeldNonPublic());
      list.add("\"updatedOn\":" + "\"" + formatDateTimeToUTCstring(aPersonal.getUpdatedOn()) + "\"");
    }
    return listToJson(list);
  }
}


//-------------------------------------------------------
// Request
//

class ReqUsername
{
  String username;
  NevisAccountUsernameType usernameType;

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public NevisAccountUsernameType getUsernameType()
  {
    return usernameType;
  }

  public void setUsernameType(NevisAccountUsernameType usernameType)
  {
    this.usernameType = usernameType;
  }
}

class ReqPassword
{
  String password;

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }
}

class ReqGoogleCaptchaResponse
{
  String googleResponse;

  public String getGoogleResponse()
  {
    return googleResponse;
  }

  public void setGoogleResponse(String googleResponse)
  {
    this.googleResponse = googleResponse;
  }
}

class ReqCreateAccount
{
  String email;
  String phone;
  String password;

  String nickName;
  String firstName;
  String middleName;
  String lastName;

  String gender;
  String dateOfBirth;

  String country;
  String city;

  String company;
  String positionHeld;

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getPhone()
  {
    return phone;
  }

  public void setPhone(String phone)
  {
    this.phone = phone;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getNickName()
  {
    return nickName;
  }

  public void setNickName(String nickName)
  {
    this.nickName = nickName;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  public String getMiddleName()
  {
    return middleName;
  }

  public void setMiddleName(String middleName)
  {
    this.middleName = middleName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

  public String getGender()
  {
    return gender;
  }

  public void setGender(String gender)
  {
    this.gender = gender;
  }

  public String getDateOfBirth()
  {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth)
  {
    this.dateOfBirth = dateOfBirth;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public String getCity()
  {
    return city;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  public String getCompany()
  {
    return company;
  }

  public void setCompany(String company)
  {
    this.company = company;
  }

  public String getPositionHeld()
  {
    return positionHeld;
  }

  public void setPositionHeld(String positionHeld)
  {
    this.positionHeld = positionHeld;
  }
}

class ReqDeleteAccount
{
  String curpass;

  String curpassFieldName = "curpass";

  public String getCurpass()
  {
    return curpass;
  }

  public void setCurpass(String curpass)
  {
    this.curpass = curpass;
  }
}

class ReqPasswordChange
{
  String curpass;
  String newpass;

  String curpassFieldName = "curpass";
  String newpassFieldName = "newpass";

  public String getCurpass()
  {
    return curpass;
  }

  public void setCurpass(String curpass)
  {
    this.curpass = curpass;
  }

  public String getNewpass()
  {
    return newpass;
  }

  public void setNewpass(String newpass)
  {
    this.newpass = newpass;
  }
}

class ReqPasswordReset
{
  public String key;
  String newpass;

  String keyFieldFullName = "confirm-key";
  String newpassField = "newpass";

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getNewpass()
  {
    return newpass;
  }

  public void setNewpass(String newpass)
  {
    this.newpass = newpass;
  }
}

class ReqConfirm
{
  public String key;

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }
}

class ReqEmail
{
  public String email;

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }
}

class ReqEmailChange
{
  String newemail;
  String curpass;

  String newemailFieldName = "newemail";
  String curpassFieldName = "curpass";

  public String getNewemail()
  {
    return newemail;
  }

  public void setNewemail(String newemail)
  {
    this.newemail = newemail;
  }

  public String getCurpass()
  {
    return curpass;
  }

  public void setCurpass(String curpass)
  {
    this.curpass = curpass;
  }
}

class ReqUpdateAccountEmail
{
  Boolean nonPublic;

  public Boolean getNonPublic()
  {
    return nonPublic;
  }

  public void setNonPublic(Boolean nonPublic)
  {
    this.nonPublic = nonPublic;
  }
}

class ReqPhoneChange
{
  String newphone;
  String curpass;

  String newphoneFieldName = "newphone";
  String curpassFieldName = "curpass";

  public String getNewphone()
  {
    return newphone;
  }

  public void setNewphone(String newphone)
  {
    this.newphone = newphone;
  }

  public String getCurpass()
  {
    return curpass;
  }

  public void setCurpass(String curpass)
  {
    this.curpass = curpass;
  }
}

class ReqUpdateAccountPhone
{
  Boolean nonPublic;

  public Boolean getNonPublic()
  {
    return nonPublic;
  }

  public void setNonPublic(Boolean nonPublic)
  {
    this.nonPublic = nonPublic;
  }
}

class ReqNickNameChange
{
  String newNickName;
  String curpass;

  String newNickNameFieldName = "newNickName";
  String curpassFieldName = "curpass";

  public String getNewNickName()
  {
    return newNickName;
  }

  public void setNewNickName(String newNickName)
  {
    this.newNickName = newNickName;
  }

  public String getCurpass()
  {
    return curpass;
  }

  public void setCurpass(String curpass)
  {
    this.curpass = curpass;
  }
}