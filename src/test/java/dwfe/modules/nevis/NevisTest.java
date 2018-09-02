package dwfe.modules.nevis;

import dwfe.db.mailing.DwfeMailing;
import dwfe.db.mailing.DwfeMailingService;
import dwfe.db.mailing.DwfeMailingType;
import dwfe.modules.nevis.config.NevisConfigProperties;
import dwfe.modules.nevis.db.account.access.NevisAccountAccess;
import dwfe.modules.nevis.db.account.access.NevisAccountAccessService;
import dwfe.modules.nevis.db.account.access.NevisAccountThirdParty;
import dwfe.modules.nevis.db.account.access.NevisAccountUsernameType;
import dwfe.modules.nevis.db.account.email.NevisAccountEmail;
import dwfe.modules.nevis.db.account.email.NevisAccountEmailService;
import dwfe.modules.nevis.db.account.personal.NevisAccountPersonal;
import dwfe.modules.nevis.db.account.personal.NevisAccountPersonalService;
import dwfe.modules.nevis.db.account.phone.NevisAccountPhone;
import dwfe.modules.nevis.db.account.phone.NevisAccountPhoneService;
import dwfe.modules.nevis.test.NevisTestAuth;
import dwfe.modules.nevis.test.NevisTestChecker;
import dwfe.modules.nevis.test.NevisTestClient;
import dwfe.modules.nevis.test.NevisTestUtil;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dwfe.db.mailing.DwfeMailingType.*;
import static dwfe.db.other.DwfeGender.M;
import static dwfe.db.other.DwfeModule.NEVIS;
import static dwfe.modules.nevis.db.account.access.NevisAccountThirdParty.GOOGLE;
import static dwfe.modules.nevis.db.account.access.NevisAccountUsernameType.*;
import static dwfe.modules.nevis.test.NevisTestAuthType.SIGN_IN;
import static dwfe.modules.nevis.test.NevisTestAuthorityLevel.USER;
import static dwfe.modules.nevis.test.NevisTestResourceAccessingType.USUAL;
import static dwfe.modules.nevis.test.NevisTestVariablesForIntegrationTest.*;
import static dwfe.modules.nevis.util.NevisUtil.*;
import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

//
// == https://spring.io/guides/gs/testing-web/
//

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT  // == https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NevisTest
{
  private static final Logger log = LoggerFactory.getLogger(NevisTest.class);
  private static Set<String> auth_test_access_tokens = new HashSet<>();

  @Autowired
  private NevisConfigProperties prop;
  @Autowired
  private NevisTestUtil util;
  @Autowired
  private NevisTestAuth auth;
  @Autowired
  private NevisTestClient client;
  @Autowired
  private NevisAccountAccessService accessService;
  @Autowired
  private NevisAccountEmailService emailService;
  @Autowired
  private NevisAccountPhoneService phoneService;
  @Autowired
  private NevisAccountPersonalService personalService;
  @Autowired
  private DwfeMailingService mailingService;


  //-------------------------------------------------------
  // Auth
  //

  @Test
  public void _01_01_USER()
  {
    logHead("USER");
    var user = auth.getUSER();
    auth_test_access_tokens.add(user.access_token);
    fullAuth(user);
  }

  @Test
  public void _01_02_ADMIN()
  {
    logHead("ADMIN");
    var admin = auth.getADMIN();
    auth_test_access_tokens.add(admin.access_token);
    fullAuth(admin);
  }

  @Test
  public void _01_03_ANY()
  {
    logHead("ANY");
    var anonymous = auth.getAnonymous();
    util.resourceAccessingProcess(anonymous.access_token, anonymous.authorityLevel, USUAL);
  }

  @Test
  public void _01_04_different_access_tokens()
  {
    logHead("list of Access Tokens");
    log.info("\n\n{}", auth_test_access_tokens.stream().collect(Collectors.joining("\n")));
    assertEquals(2, auth_test_access_tokens.size());
  }

  @Test
  public void _01_05_signIn()
  {
    logHead("Sign-In");

    var clientTrusted = client.getClientTrusted();

    // EMAIL, usernameType=null
    util.tokenProcess(SIGN_IN, auth.of(Account2_EMAIL, null, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_EMAIL + "1", null, Account2_Pass, clientTrusted), 400);
    // NICKNAME, usernameType=null
    util.tokenProcess(SIGN_IN, auth.of(Account2_NICKNAME, null, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_NICKNAME + "1", null, Account2_Pass, clientTrusted), 400);
    // PHONE, usernameType=null
    util.tokenProcess(SIGN_IN, auth.of(Account2_PHONE, null, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_PHONE + "1", null, Account2_Pass, clientTrusted), 400);
    // ID, usernameType=null
    util.tokenProcess(SIGN_IN, auth.of(Account2_ID, null, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_ID + "1", null, Account2_Pass, clientTrusted), 400);

    // EMAIL, usernameType=EMAIL
    util.tokenProcess(SIGN_IN, auth.of(Account2_EMAIL, EMAIL, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_EMAIL + "1", EMAIL, Account2_Pass, clientTrusted), 400);
    // NICKNAME, usernameType=NICKNAME
    util.tokenProcess(SIGN_IN, auth.of(Account2_NICKNAME, NICKNAME, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_NICKNAME + "1", NICKNAME, Account2_Pass, clientTrusted), 400);
    // PHONE, usernameType=PHONE
    util.tokenProcess(SIGN_IN, auth.of(Account2_PHONE, PHONE, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_PHONE + "1", PHONE, Account2_Pass, clientTrusted), 400);
    // ID, usernameType=ID
    util.tokenProcess(SIGN_IN, auth.of(Account2_ID, ID, Account2_Pass, clientTrusted), 200);
    util.tokenProcess(SIGN_IN, auth.of(Account2_ID + "1", ID, Account2_Pass, clientTrusted), 400);
  }


  //-------------------------------------------------------
  // Account.Common
  //

  @Test
  public void _02_01_canUseUsername()
  {
    logHead("Can use Username");
    util.check(POST, prop.getResource().getCanUseUsername(), auth.getAnonym_accessToken(), checkers_for_canUseUsername);
  }

  @Test
  public void _02_02_canUsePassword()
  {
    logHead("Can use Password");
    util.check(POST, prop.getResource().getCanUsePassword(), auth.getAnonym_accessToken(), checkers_for_canUsePassword);
  }

  @Test
  public void _02_03_googleCaptchaValidate()
  {
    logHead("Google Captcha Validate");
    util.check(POST, prop.getResource().getGoogleCaptchaValidate(), auth.getAnonym_accessToken(), checkers_for_googleCaptchaValidate);
  }

  @Test
  public void _02_04_createAccount()
  {
    logHead("Create Account");

    util.check(POST, prop.getResource().getCreateAccount(), auth.getAnonym_accessToken(), checkers_for_createAccount());
    //
    // Was created 5 new accounts:
    //  - Account3 - EMAIL, password was not passed
    //  - Account4 - NICKNAME, password was passed
    //  - Account5 - PHONE, already encoded password was passed
    //  - Account6 - EMAIL, password was passed + Third-party
    //  - Account7 - EMAIL, values for check restrictions


    // Account3
    var aAccess1 = checkAccountAccessAfterCreateAccount(Account3_EMAIL, EMAIL, null);
    var id = aAccess1.getId();
    var aEmail = getAccountEmailById(id, true);
    checkAccountEmail(aEmail, Account3_EMAIL, true, true);
    getAccountPhoneById(id, false);
    checkAccountPersonal_ExactMatch(id, NevisAccountPersonal.of(
            null, true,       // nickName
            "ozon", true,     // firstName
            "Alice", true,    // middleName
            "sunshine", true, // lastName
            null, true,       // gender
            null, true,       // dateOfBirth
            null, true,       // country
            null, true,       // city
            null, true,       // company
            null, true)       // positionHeld
    );
    var mailing = getMailingFirstOfOne(WELCOME_PASSWORD, aEmail.getValue());
    var password = mailing.getData();
    assertTrue(password.length() >= 9);
    Account3_Pass = password; // for next tests


    // Account4
    var aAccess2 = checkAccountAccessAfterCreateAccount(Account4_NICKNAME, NICKNAME, null);
    id = aAccess2.getId();
    Account4_ID = id;
    getAccountEmailById(id, false);
    getAccountPhoneById(id, false);
    checkAccountPersonal_ExactMatch(id, NevisAccountPersonal.of(
            Account4_NICKNAME, true,              // nickName
            null, true,                           // firstName
            null, true,                           // middleName
            null, true,                           // lastName
            M, true,                              // gender
            LocalDate.parse("1980-11-27"), true,  // dateOfBirth
            "DE", true,                           // country
            null, true,                           // city
            null, true,                           // company
            null, true)                           // positionHeld
    );
    var mailingList = mailingService.findByEmail(Account4_EMAIL);
    assertEquals(0, mailingList.size());


    // Account5
    var aAccess3 = checkAccountAccessAfterCreateAccount(Account5_PHONE, PHONE, null);
    id = aAccess3.getId();
    getAccountEmailById(id, false);
    var aPhone = getAccountPhoneById(id, true);
    checkAccountPhone(aPhone, Account5_PHONE, true, false);
    checkAccountPersonal_ExactMatch(id, NevisAccountPersonal.of(
            null, true,           // nickName
            null, true,           // firstName
            null, true,           // middleName
            null, true,           // lastName
            null, true,           // gender
            null, true,           // dateOfBirth
            null, true,           // country
            "Dallas", true,       // city
            "Home Ltd.", true,    // company
            "programmer", true)   // positionHeld
    );
    mailingList = mailingService.findByEmail(Account5_EMAIL);
    assertEquals(0, mailingList.size());


    // Account6
    var aAccess4 = checkAccountAccessAfterCreateAccount(Account6_EMAIL, EMAIL, GOOGLE);
    id = aAccess4.getId();
    Account6_ID = id;
    aEmail = getAccountEmailById(id, true);
    checkAccountEmail(aEmail, Account6_EMAIL, true, false);
    getAccountPhoneById(id, false);
    checkAccountPersonal_ExactMatch(id, NevisAccountPersonal.of(
            null, true, // nickName
            null, true, // firstName
            null, true, // middleName
            null, true, // lastName
            null, true, // gender
            null, true, // dateOfBirth
            null, true, // country
            null, true, // city
            null, true, // company
            null, true) // positionHeld
    );
    mailing = getMailingFirstOfOne(WELCOME_ONLY, aEmail.getValue());
    assertEquals(0, mailing.getData().length());


    // Account7
    var aAccess5 = checkAccountAccessAfterCreateAccount(Account7_EMAIL, EMAIL, null);
    id = aAccess5.getId();
    Account7_ID = id;
    aEmail = getAccountEmailById(id, true);
    checkAccountEmail(aEmail, Account7_EMAIL, true, false);
    aPhone = getAccountPhoneById(id, true);
    checkAccountPhone(aPhone, "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true, false);
    checkAccountPersonal_ExactMatch(id, NevisAccountPersonal.of(
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true, // nickName
            "12345678901234567890", true, // firstName
            "12345678901234567890", true, // middleName
            "12345678901234567890", true, // lastName
            null, true,                   // gender
            null, true,                   // dateOfBirth
            null, true,                   // country
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true, // city
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true, // company
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true) // positionHeld
    );
    mailing = getMailingFirstOfOne(WELCOME_ONLY, aEmail.getValue());
    assertEquals(0, mailing.getData().length());


    // perform full auth test for newly created accounts
    var clientTrusted = client.getClientTrusted();
    fullAuth(auth.of(USER, aAccess1.getUsername(), aAccess1.getUsernameType(), Account3_Pass, clientTrusted));
    fullAuth(auth.of(USER, aAccess2.getUsername(), aAccess2.getUsernameType(), Account4_Pass, clientTrusted));
    fullAuth(auth.of(USER, aAccess3.getUsername(), aAccess3.getUsernameType(), Account5_Pass, clientTrusted));
    fullAuth(auth.of(USER, aAccess4.getUsername(), aAccess4.getUsernameType(), Account6_Pass, clientTrusted));
    fullAuth(auth.of(USER, aAccess5.getUsername(), aAccess5.getUsernameType(), Account7_Pass_Decoded, clientTrusted));
  }

  @Test
  public void _02_05_id()
  {
    logHead("ID");

    var resource = "/id/";

    util.check(GET, resource + "1", auth.getAnonym_accessToken(), checkers_for_id1);
    util.check(GET, resource + Account1_ID, auth.getAnonym_accessToken(), checkers_for_id2);
    util.check(GET, resource + Account2_ID, auth.getAnonym_accessToken(), checkers_for_id3);
  }

  @Test
  public void _02_06_thirdPartyAuth()
  {
    logHead("Third-party Auth");
    util.check(POST, prop.getResource().getThirdPartyAuth(), auth.getAnonym_accessToken(), checkers_for_thirdPartyAuth);
  }

  @Test
  public void _99_01_deleteAccount()
  {
    logHead("Delete Account");

    var auth1 = auth.of(USER, Account1_EMAIL, EMAIL, Account1_Pass, client.getClientTrusted());

    util.check(POST, prop.getResource().getDeleteAccount(), auth1.access_token, checkers_for_deleteAccount);

    assertFalse(accessService.findById(Long.parseLong(Account1_ID)).isPresent());
    assertFalse(emailService.findById(Long.parseLong(Account1_ID)).isPresent());
    assertFalse(phoneService.findById(Long.parseLong(Account1_ID)).isPresent());
    assertFalse(personalService.findById(Long.parseLong(Account1_ID)).isPresent());
  }


  //-------------------------------------------------------
  // Account.Access
  //

  @Test
  public void _03_01_getAccountAccess()
  {
    logHead("Get Account Access");

    var resource = prop.getResource().getGetAccountAccess();

    util.check(GET, resource, auth.getADMIN().access_token, checkers_for_getAccountAccess1);

    var auth1 = auth.of(USER, Account6_EMAIL, EMAIL, Account6_Pass, client.getClientTrusted());
    util.check(GET, resource, auth1.access_token, checkers_for_getAccountAccess2(Account6_ID));
  }

  @Test
  public void _03_02_passwordChange()
  {
    logHead("Password Change");

    passwordChange(Account6_EMAIL, EMAIL, Account6_Pass, Account6_NewPass, checkers_for_passwordChange);
    passwordChange(Account7_EMAIL, EMAIL, Account7_Pass_Decoded, Account7_NewPass_Decoded, checkers_for_passwordChange_2);
  }

  private void passwordChange(String username, NevisAccountUsernameType usernameType, String curpass, String newpass, List<NevisTestChecker> checkers)
  {
    logHead("Password Change = " + username);

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getPasswordChange();

    var auth1 = auth.of(username, usernameType, newpass, clientTrusted);
    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1.password = curpass;
    util.tokenProcess(SIGN_IN, auth1, 200);

    util.check(POST, resource, auth1.access_token, checkers);

    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1.password = newpass;
    util.tokenProcess(SIGN_IN, auth1, 200);

    fullAuth(auth1);
  }

  @Test
  public void _03_03_passwordReset()
  {
    logHead("Password Reset");

    performPasswordReset(Account6_EMAIL, EMAIL, Account6_NewPass, Account6_Pass, Account6_Pass);
    performPasswordReset(Account7_EMAIL, EMAIL, Account7_NewPass_Decoded, Account7_Pass_Decoded, Account7_Pass_Encoded);
  }

  private void performPasswordReset(String username, NevisAccountUsernameType usernameType, String curpass, String newpass, String newpassForCheckers)
  {
    passwordResetReq(username);
    passwordReset(username, usernameType, curpass, newpass, newpassForCheckers);
  }

  private void passwordResetReq(String username)
  {
    logHead("Password Reset Request = " + username);

    var resource = prop.getResource().getPasswordResetReq();
    var timeToWait = TimeUnit.MILLISECONDS.toSeconds(prop.getScheduledTaskMailing().getCollectFromDbInterval()) * 3;
    var type = PASSWORD_RESET_CONFIRM;

    assertEquals(0, mailingService.findByTypeAndEmail(type, username).size());

    util.check(POST, resource, null, checkers_for_passwordResetReq(username));
    util.check(POST, resource, null, checkers_for_passwordResetReq_duplicateDelay(username));

    pleaseWait(timeToWait);
    var letter = getMailingFirstOfOne(type, username);
    assertFalse(letter.isMaxAttemptsReached());
    assertTrue(letter.getData().length() >= 28);

    util.check(POST, resource, null, checkers_for_passwordResetReq(username));

    pleaseWait(timeToWait);
    assertEquals(2, mailingService.findSentNotEmptyData(type, username).size());
  }

  private void passwordReset(String username, NevisAccountUsernameType usernameType, String curpass, String newpass, String newpassForCheckers)
  {
    logHead("Password Reset = " + username);

    var type = PASSWORD_RESET_CONFIRM;
    var clientTrusted = client.getClientTrusted();

    var mailingList = mailingService.findSentNotEmptyData(type, username);
    assertEquals(2, mailingList.size());

    var auth1 = auth.of(username, usernameType, newpass, clientTrusted);
    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1.password = curpass;
    util.tokenProcess(SIGN_IN, auth1, 200);

    //change password
    util.check(POST, prop.getResource().getPasswordReset(), null,
            checkers_for_passwordReset(username, newpassForCheckers, mailingList.get(0).getData()));

    assertEquals(1, mailingService.findSentNotEmptyData(type, username).size());

    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1.password = newpass;
    util.tokenProcess(SIGN_IN, auth1, 200);

    fullAuth(auth1);
  }


  //-------------------------------------------------------
  // Account.Email
  //

  @Test
  public void _04_01_getAccountEmail()
  {
    logHead("Get Account Email");

    var resource = prop.getResource().getGetAccountEmail();
    var auth0 = auth.of(USER, Account4_NICKNAME, NICKNAME, Account4_Pass, client.getClientTrusted());

    util.check(GET, resource, auth0.access_token, checkers_for_getAccountEmail1);
    util.check(GET, resource, auth.getADMIN().access_token, checkers_for_getAccountEmail2);
  }

  @Test
  public void _04_02_emailConfirmReq()
  {
    logHead("Email Confirm Request");

    var timeToWait = TimeUnit.MILLISECONDS.toSeconds(prop.getScheduledTaskMailing().getCollectFromDbInterval()) * 2;
    var resource = prop.getResource().getEmailConfirmReq();
    var clientTrusted = client.getClientTrusted();
    var type = EMAIL_CONFIRM;

    mailingService.deleteAll();

    // (0) check error 'no-email-associated-with-account'
    var auth0 = auth.of(USER, Account4_NICKNAME, NICKNAME, Account4_Pass, clientTrusted);
    util.check(GET, resource, auth0.access_token, checkers_for_emailConfirmReq_noEmailAssociatedWithAccount);

    // (1) check error 'email-is-already-confirmed'
    var auth_USER = auth.getUSER();
    var aEmail = getAccountEmailByEmail(Account2_EMAIL);
    assertTrue(aEmail.isConfirmed());
    util.check(GET, resource, auth_USER.access_token, checkers_for_emailConfirmReq_isAlreadyConfirmed);

    // (2) successful confirmation request
    var email = Account6_EMAIL;
    var auth1 = auth.of(USER, Account6_EMAIL, EMAIL, Account6_Pass, clientTrusted);
    var accessToken = auth1.access_token;
    aEmail = getAccountEmailByEmail(email);
    assertFalse(aEmail.isConfirmed());

    var mailingList = getMailingListByTypeAndEmail(type, email);
    assertEquals(0, mailingList.size());
    util.check(GET, resource, accessToken, checkers_for_emailConfirmReq);

    var mailing = getMailingFirstOfOne(type, email);
    assertFalse(mailing.isSent());
    assertFalse(mailing.isMaxAttemptsReached());
    assertTrue(mailing.getData().length() >= 35);

    // (3)
    // Ok. At the moment we have 1 key and it is not yet time to send a duplicate request
    // Let's try to add one more key ==> сheck for 'delay-between-duplicate-requests' error
    util.check(GET, resource, accessToken, checkers_for_emailConfirmReq_duplicateDelay);

    pleaseWait(timeToWait);
    mailing = getMailingLastSentNotEmptyData(type, email);
    assertFalse(mailing.isMaxAttemptsReached());
    var alreadySentKeyOld = mailing.getData();

    // (4)
    // At the moment we have a key that has already been sent and is waiting for confirmation
    // Try to add one more key, because duplicate delay should already expire
    util.check(GET, resource, accessToken, checkers_for_emailConfirmReq);
    util.check(GET, resource, accessToken, checkers_for_emailConfirmReq_duplicateDelay);

    pleaseWait(timeToWait);
    mailing = getMailingLastSentNotEmptyData(type, email);
    assertFalse(mailing.isMaxAttemptsReached());
    assertNotEquals(alreadySentKeyOld, mailing.getData());  // another new key was success added and sent

    aEmail = getAccountEmailByEmail(email);
    assertFalse(aEmail.isConfirmed());
  }

  @Test
  public void _04_03_emailConfirm()
  {
    logHead("Email Confirm");

    var type = EMAIL_CONFIRM;
    var email = Account6_EMAIL;

    var aEmail = getAccountEmailByEmail(email);
    assertFalse(aEmail.isConfirmed());

    var mailingList = getMailingSentNotEmptyData(type, email);
    assertEquals(2, mailingList.size());
    var confirmKey = mailingList.get(0).getData();
    util.check(POST, prop.getResource().getEmailConfirm(), null, checkers_for_confirmEmail(confirmKey));

    mailingList = getMailingSentNotEmptyData(type, email);
    assertEquals(1, mailingList.size());

    aEmail = getAccountEmailByEmail(email);
    assertTrue(aEmail.isConfirmed());
  }

  @Test
  public void _04_04_emailChange()
  {
    logHead("Email Change");

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getEmailChange();

    var auth1 = auth.of(Account5_EMAIL, EMAIL, Account5_Pass, clientTrusted);
    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1 = auth.of(USER, Account5_PHONE, PHONE, Account5_Pass, clientTrusted);

    var auth2 = auth.of(Account6_NewEmail, EMAIL, Account6_Pass, clientTrusted);
    util.tokenProcess(SIGN_IN, auth2, 400);
    auth2.username = Account6_EMAIL;
    util.tokenProcess(SIGN_IN, auth2, 200);
    var aEmail = getAccountEmailByEmail(auth2.username);
    checkAccountEmail(aEmail, auth2.username, true, true);

    util.check(POST, resource, auth1.access_token, checkers_for_emailChange1);
    util.check(POST, resource, auth2.access_token, checkers_for_emailChange2);

    auth1 = auth.of(USER, Account5_PHONE, PHONE, Account5_Pass, clientTrusted);
    auth1 = auth.of(USER, Account5_EMAIL, EMAIL, Account5_Pass, clientTrusted);
    aEmail = getAccountEmailByEmail(auth1.username);
    checkAccountEmail(aEmail, auth1.username, true, false);

    util.tokenProcess(SIGN_IN, auth2, 400);
    auth2.username = Account6_NewEmail;
    util.tokenProcess(SIGN_IN, auth2, 200);
    aEmail = getAccountEmailByEmail(auth2.username);
    checkAccountEmail(aEmail, auth2.username, true, false);
  }

  @Test
  public void _04_05_updateAccountEmail()
  {
    logHead("Update Account Email");

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getUpdateAccountEmail();

    var auth1 = auth.of(USER, Account4_NICKNAME, NICKNAME, Account4_Pass, clientTrusted);
    var auth2 = auth.of(USER, Account5_PHONE, PHONE, Account5_Pass, clientTrusted);

    var aEmail = getAccountEmailByEmail(Account5_EMAIL);
    checkAccountEmail(aEmail, Account5_EMAIL, true, false);

    util.check(POST, resource, auth1.access_token, checkers_for_updateAccountEmail1);
    util.check(POST, resource, auth2.access_token, checkers_for_updateAccountEmail2);

    aEmail = getAccountEmailByEmail(Account5_EMAIL);
    checkAccountEmail(aEmail, Account5_EMAIL, false, false);
  }


  //-------------------------------------------------------
  // Account.Phone
  //

  @Test
  public void _05_01_getAccountPhone()
  {
    logHead("Get Account Phone");

    var resource = prop.getResource().getGetAccountPhone();
    var clientTrusted = client.getClientTrusted();

    var auth1 = auth.of(USER, Account4_NICKNAME, NICKNAME, Account4_Pass, clientTrusted);
    var auth2 = auth.of(USER, Account5_PHONE, PHONE, Account5_Pass, clientTrusted);

    util.check(GET, resource, auth1.access_token, checkers_for_getAccountPhone1);
    util.check(GET, resource, auth2.access_token, checkers_for_getAccountPhone2);
  }

  @Test
  public void _05_02_phoneChange()
  {
    logHead("Phone Change");

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getPhoneChange();

    var auth1 = auth.of(Account7_PHONE, PHONE, Account7_Pass_Decoded, clientTrusted);
    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1 = auth.of(USER, Account7_EMAIL, EMAIL, Account7_Pass_Decoded, clientTrusted);

    var auth2 = auth.of(Account5_NewPhone, PHONE, Account5_Pass, clientTrusted);
    util.tokenProcess(SIGN_IN, auth2, 400);
    auth2.username = Account5_PHONE;
    util.tokenProcess(SIGN_IN, auth2, 200);
    var aPhone = getAccountPhoneByPhone(Account5_PHONE, true);
    checkAccountPhone(aPhone, Account5_PHONE, true, false);

    util.check(POST, resource, auth1.access_token, checkers_for_phoneChange1);
    util.check(POST, resource, auth2.access_token, checkers_for_phoneChange2);

    auth1 = auth.of(USER, Account7_PHONE, PHONE, Account7_Pass_Decoded, clientTrusted);
    aPhone = getAccountPhoneByPhone(Account7_PHONE, true);
    checkAccountPhone(aPhone, Account7_PHONE, true, false);

    util.tokenProcess(SIGN_IN, auth2, 400);
    auth2.username = Account5_NewPhone;
    util.tokenProcess(SIGN_IN, auth2, 200);
    aPhone = getAccountPhoneByPhone(Account5_NewPhone, true);
    checkAccountPhone(aPhone, Account5_NewPhone, true, false);
  }

  @Test
  public void _05_03_updateAccountPhone()
  {
    logHead("Update Account Phone");

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getUpdateAccountPhone();

    var auth1 = auth.of(USER, Account3_EMAIL, EMAIL, Account3_Pass, clientTrusted);
    var auth2 = auth.of(USER, Account5_NewPhone, PHONE, Account5_Pass, clientTrusted);

    var aPhone = getAccountPhoneByPhone(Account5_NewPhone, true);
    checkAccountPhone(aPhone, Account5_NewPhone, true, false);

    util.check(POST, resource, auth1.access_token, checkers_for_updateAccountPhone1);
    util.check(POST, resource, auth2.access_token, checkers_for_updateAccountPhone2);

    aPhone = getAccountPhoneByPhone(Account5_NewPhone, true);
    checkAccountPhone(aPhone, Account5_NewPhone, false, false);
  }


  //-------------------------------------------------------
  // Account.Personal
  //

  @Test
  public void _06_01_getAccountPersonal()
  {
    logHead("Get Account Personal");

    var resource = prop.getResource().getGetAccountPersonal();
    var clientTrusted = client.getClientTrusted();

    var auth1 = auth.of(USER, Account6_NewEmail, EMAIL, Account6_Pass, clientTrusted);
    var auth2 = auth.of(USER, Account7_EMAIL, EMAIL, Account7_Pass_Decoded, clientTrusted);
    var auth3 = auth.of(USER, Account4_NICKNAME, NICKNAME, Account4_Pass, clientTrusted);

    util.check(GET, resource, auth1.access_token, checkers_for_getAccountPersonal1);
    util.check(GET, resource, auth2.access_token, checkers_for_getAccountPersonal2);
    util.check(GET, resource, auth3.access_token, checkers_for_getAccountPersonal3);
  }

  @Test
  public void _06_02_nicknameChange()
  {
    logHead("NickName Change");

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getNicknameChange();

    var auth1 = auth.of(Account4_NewNickName, NICKNAME, Account4_Pass, clientTrusted);
    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1.username = Account4_NICKNAME;
    util.tokenProcess(SIGN_IN, auth1, 200);

    var auth2 = auth.of(Account7_NICKNAME, NICKNAME, Account7_Pass_Decoded, clientTrusted);
    util.tokenProcess(SIGN_IN, auth2, 400);
    auth2 = auth.of(USER, Account7_EMAIL, EMAIL, Account7_Pass_Decoded, clientTrusted);

    util.check(POST, resource, auth1.access_token, checkers_for_nicknameChange1);
    util.check(POST, resource, auth2.access_token, checkers_for_nicknameChange2);

    util.tokenProcess(SIGN_IN, auth1, 400);
    auth1.username = Account4_NewNickName;
    util.tokenProcess(SIGN_IN, auth1, 200);
    checkAccountPersonal_ExactMatch(Account4_ID, NevisAccountPersonal.of(
            Account4_NewNickName, true,           // nickName
            null, true,                           // firstName
            null, true,                           // middleName
            null, true,                           // lastName
            M, true,                              // gender
            LocalDate.parse("1980-11-27"), true,  // dateOfBirth
            "DE", true,                           // country
            null, true,                           // city
            null, true,                           // company
            null, true)                           // positionHeld
    );

    auth2 = auth.of(USER, Account7_NICKNAME, NICKNAME, Account7_Pass_Decoded, clientTrusted);
    checkAccountPersonal_ExactMatch(Account7_ID, NevisAccountPersonal.of(
            Account7_NICKNAME, true,      // nickName
            "12345678901234567890", true, // firstName
            "12345678901234567890", true, // middleName
            "12345678901234567890", true, // lastName
            null, true,                   // gender
            null, true,                   // dateOfBirth
            null, true,                   // country
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true, // city
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true, // company
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true) // positionHeld
    );
  }

  @Test
  public void _06_03_updateAccountPersonal()
  {
    logHead("Update Account Personal");

    var clientTrusted = client.getClientTrusted();
    var resource = prop.getResource().getUpdateAccountPersonal();

    var auth1 = auth.of(USER, Account6_NewEmail, EMAIL, Account6_Pass, clientTrusted);

    util.check(POST, resource, auth1.access_token, checkers_for_updateAccountPersonal1);
    checkAccountPersonal_ExactMatch(Account6_ID, NevisAccountPersonal.of(
            null, false,                           // nickName
            "12345678901234567890", false,         // firstName
            "12345678901234567890", false,         // middleName
            "12345678901234567890", false,         // lastName
            M, false,                              // gender
            LocalDate.parse("2018-07-11"), false,  // dateOfBirth
            "RU", false,                           // country
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", false, // city
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", false, // company
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", false) // positionHeld
    );

    util.check(POST, resource, auth1.access_token, checkers_for_updateAccountPersonal2);
    checkAccountPersonal_ExactMatch(Account6_ID, NevisAccountPersonal.of(
            null, true, // nickName
            null, true, // firstName
            null, true, // middleName
            null, true, // lastName
            null, true, // gender
            null, true, // dateOfBirth
            null, true, // country
            null, true, // city
            null, true, // company
            null, true) // positionHeld
    );
  }


  //-------------------------------------------------------
  // UTILs
  //

  private void fullAuth(NevisTestAuth auth)
  {
    util.fullAuthProcess(auth);
    mailingService.deleteAll();
  }

  private NevisAccountAccess checkAccountAccessAfterCreateAccount(String username, NevisAccountUsernameType usernameType, NevisAccountThirdParty thirdParty)
  {
    var aAccess = getAccessByUsername(username, usernameType);

    assertTrue(aAccess.getId() > 999);
    assertEquals(username, aAccess.getUsername());
    assertEquals(usernameType, aAccess.getUsernameType());
    if (EMAIL == usernameType)
      assertTrue(isEmailValid(aAccess.getUsername(), "email", new ArrayList<>()));
    else if (NICKNAME == usernameType)
    {
      // ???
    }
    else if (PHONE == usernameType)
    {
      // ???
    }
    else if (ID == usernameType)
      assertTrue(isIdValid(aAccess.getUsername(), new ArrayList<>()));

    assertTrue(isStringBcrypted(aAccess.getPassword().replace("{bcrypt}", "")));

    assertNotEquals(null, aAccess.getCreatedOn());
    assertNotEquals(null, aAccess.getUpdatedOn());

    var authorities = aAccess.getAuthorities();
    assertEquals(1, authorities.size());
    assertEquals("USER", authorities.iterator().next().getAuthority());

    assertEquals(thirdParty, aAccess.getThirdParty());

    assertTrue(aAccess.isAccountNonExpired());
    assertTrue(aAccess.isCredentialsNonExpired());
    assertTrue(aAccess.isAccountNonLocked());
    assertTrue(aAccess.isEnabled());

    return aAccess;
  }

  private NevisAccountAccess getAccessByUsername(String username, NevisAccountUsernameType usernameType)
  {
    var aAccessOpt = accessService.findByUsername(username, usernameType);
    assertTrue(aAccessOpt.isPresent());
    return aAccessOpt.get();
  }

  private void checkAccountEmail(NevisAccountEmail aEmail, String expectedValue, boolean expectedNonPublic, boolean expectedConfirmed)
  {
    var email = aEmail.getValue();
    assertEquals(expectedValue, email);
    assertTrue(isEmailValid(email, "email", List.of()));
    assertEquals(expectedConfirmed, aEmail.isConfirmed());
    assertEquals(expectedNonPublic, aEmail.isNonPublic());
  }

  private NevisAccountEmail getAccountEmailById(Long id, boolean expectedIsPresent)
  {
    var aEmailOpt = emailService.findById(id);
    assertEquals(expectedIsPresent, aEmailOpt.isPresent());
    return expectedIsPresent ? aEmailOpt.get() : null;
  }

  private NevisAccountEmail getAccountEmailByEmail(String email)
  {
    var aEmailOpt = emailService.findByValue(email);
    assertTrue(aEmailOpt.isPresent());
    return aEmailOpt.get();
  }

  private void checkAccountPhone(NevisAccountPhone aPhone, String expectedValue, boolean expectedNonPublic, boolean expectedConfirmed)
  {
    var phone = aPhone.getValue();
    assertEquals(expectedValue, phone);
    assertEquals(expectedConfirmed, aPhone.isConfirmed());
    assertEquals(expectedNonPublic, aPhone.isNonPublic());
  }

  private NevisAccountPhone getAccountPhoneById(Long id, boolean expectedIsPresent)
  {
    var aPhoneOpt = phoneService.findById(id);
    assertEquals(expectedIsPresent, aPhoneOpt.isPresent());
    return expectedIsPresent ? aPhoneOpt.get() : null;
  }

  private NevisAccountPhone getAccountPhoneByPhone(String phone, boolean expectedIsPresent)
  {
    var aPhoneOpt = phoneService.findByValue(phone);
    assertEquals(expectedIsPresent, aPhoneOpt.isPresent());
    return expectedIsPresent ? aPhoneOpt.get() : null;
  }

  private void checkAccountPersonal_ExactMatch(Long id, NevisAccountPersonal tPersonal)
  {
    var aPersonal = getAccountPersonalById(id);
    assertNotEquals(null, aPersonal.getUpdatedOn());

    var tNickName = tPersonal.getNickName();
    var tNickNameNonPublic = tPersonal.getNickNameNonPublic();

    var tFirstName = tPersonal.getFirstName();
    var tFirstNameNonPublic = tPersonal.getFirstNameNonPublic();

    var tMiddleName = tPersonal.getMiddleName();
    var tMiddleNameNonPublic = tPersonal.getMiddleNameNonPublic();

    var tLastName = tPersonal.getLastName();
    var tLastNameNonPublic = tPersonal.getLastNameNonPublic();

    var tGender = tPersonal.getGender();
    var tGenderNonPublic = tPersonal.getGenderNonPublic();

    var tDateOfBirth = tPersonal.getDateOfBirth();
    var tDateOfBirthNonPublic = tPersonal.getDateOfBirthNonPublic();

    var tCountry = tPersonal.getCountry();
    var tCountryNonPublic = tPersonal.getCountryNonPublic();

    var tCity = tPersonal.getCity();
    var tCityNonPublic = tPersonal.getCityNonPublic();

    var tCompany = tPersonal.getCompany();
    var tCompanyNonPublic = tPersonal.getCompanyNonPublic();

    var tPositionHeld = tPersonal.getPositionHeld();
    var tPositionHeldNonPublic = tPersonal.getPositionHeldNonPublic();

    assertEquals(tNickName, aPersonal.getNickName());
    assertEquals(tNickNameNonPublic, aPersonal.getNickNameNonPublic());

    assertEquals(tFirstName, aPersonal.getFirstName());
    assertEquals(tFirstNameNonPublic, aPersonal.getFirstNameNonPublic());

    assertEquals(tMiddleName, aPersonal.getMiddleName());
    assertEquals(tMiddleNameNonPublic, aPersonal.getMiddleNameNonPublic());

    assertEquals(tLastName, aPersonal.getLastName());
    assertEquals(tLastNameNonPublic, aPersonal.getLastNameNonPublic());

    assertEquals(tGender, aPersonal.getGender());
    assertEquals(tGenderNonPublic, aPersonal.getGenderNonPublic());

    assertEquals(tDateOfBirth, aPersonal.getDateOfBirth());
    assertEquals(tDateOfBirthNonPublic, aPersonal.getDateOfBirthNonPublic());

    assertEquals(tCountry, aPersonal.getCountry());
    assertEquals(tCountryNonPublic, aPersonal.getCountryNonPublic());

    assertEquals(tCity, aPersonal.getCity());
    assertEquals(tCityNonPublic, aPersonal.getCityNonPublic());

    assertEquals(tCompany, aPersonal.getCompany());
    assertEquals(tCompanyNonPublic, aPersonal.getCompanyNonPublic());

    assertEquals(tPositionHeld, aPersonal.getPositionHeld());
    assertEquals(tPositionHeldNonPublic, aPersonal.getPositionHeldNonPublic());
  }

  private NevisAccountPersonal getAccountPersonalById(Long id)
  {
    var aPersonalOpt = personalService.findById(id);
    assertTrue(aPersonalOpt.isPresent());
    return aPersonalOpt.get();
  }

  private List<DwfeMailing> getMailingListByTypeAndEmail(DwfeMailingType type, String email)
  {
    return mailingService.findByTypeAndEmail(type, email);
  }

  private DwfeMailing getMailingLastSentNotEmptyData(DwfeMailingType type, String email)
  {
    var mailingOpt = mailingService.findLastSentNotEmptyData(type, email);
    assertTrue(mailingOpt.isPresent());
    var letter = mailingOpt.get();
    assertEquals(NEVIS, letter.getModule());
    return letter;
  }

  private List<DwfeMailing> getMailingSentNotEmptyData(DwfeMailingType type, String email)
  {
    var list = mailingService.findSentNotEmptyData(type, email);
    list.forEach(next -> assertEquals(NEVIS, next.getModule()));
    return list;
  }

  private DwfeMailing getMailingFirstOfOne(DwfeMailingType type, String email)
  {
    var mailing = getMailingListByTypeAndEmail(type, email);
    assertEquals(1, mailing.size());
    var letter = mailing.get(0);
    assertEquals(NEVIS, letter.getModule());
    return letter;
  }

  private void pleaseWait(long timeToWait)
  {
    try
    {
      log.info("::> please wait {} seconds...", timeToWait);
      TimeUnit.SECONDS.sleep(timeToWait);
      log.info("::> continue execution after waiting");
    }
    catch (InterruptedException ignored)
    {
    }
  }

  private static void logHead(String who)
  {
    log.info("\n=============================="
            + "\n {} "
            + "\n------------------------------", who);
  }
}
