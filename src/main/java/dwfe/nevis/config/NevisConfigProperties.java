package dwfe.nevis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static dwfe.nevis.util.NevisUtil.formatMillisecondsToReadableString;

@Validated
@Configuration
@ConfigurationProperties(prefix = "dwfe.nevis")
public class NevisConfigProperties implements InitializingBean
{
  // == http://www.baeldung.com/configuration-properties-in-spring-boot

  private final static Logger log = LoggerFactory.getLogger(NevisConfigProperties.class);

  @NotBlank
  private String api;

  private Resource resource = new Resource();

  private GoogleCaptcha googleCaptcha;

  @NotNull
  private Frontend frontend = new Frontend();

  @NotNull
  private ScheduledTaskMailing scheduledTaskMailing;

  @NotNull
  private OAuth2ClientTrusted oauth2ClientTrusted;

  @NotNull
  private OAuth2ClientUntrusted oauth2ClientUntrusted;

  @Override
  public void afterPropertiesSet() throws Exception
  {
    if (scheduledTaskMailing.getTimeoutForDuplicateRequest() <= 0)
      scheduledTaskMailing.setTimeoutForDuplicateRequest(
              scheduledTaskMailing.getSendInterval() * scheduledTaskMailing.getMaxAttemptsToSendIfError());

    log.info(toString());
  }

  public static class Resource
  {
    // Auth
    private String signIn = "/sign-in";
    private String signOut = "/sign-out";

    // Account.Common
    private String canUseUsername = "/can-use-username";
    private String canUsePassword = "/can-use-password";
    private String googleCaptchaValidate = "/google-captcha-validate";
    private String createAccount = "/create-account";
    private String id = "/id/{id}";
    private String deleteAccount = "/delete-account";

    // Account.Access
    private String getAccountAccess = "/get-account-access";
    private String passwordChange = "/password-change";
    private String passwordResetReq = "/password-reset-req";
    private String passwordResetConfirm = "/password-reset-confirm";
    private String passwordReset = "/password-reset";

    // Account.Email
    private String getAccountEmail = "/get-account-email";
    private String emailConfirmReq = "/email-confirm-req";
    private String emailConfirm = "/email-confirm";
    private String emailChange = "/email-change";
    private String updateAccountEmail = "/update-account-email";

    // Account.Phone
    private String getAccountPhone = "/get-account-phone";
    private String phoneChange = "/phone-change";
    private String updateAccountPhone = "/update-account-phone";

    // Account.Personal
    private String getAccountPersonal = "/get-account-personal";
    private String nicknameChange = "/nickname-change";
    private String updateAccountPersonal = "/update-account-personal";


    public String getSignIn()
    {
      return signIn;
    }

    public void setSignIn(String signIn)
    {
      this.signIn = signIn;
    }

    public String getSignOut()
    {
      return signOut;
    }

    public void setSignOut(String signOut)
    {
      this.signOut = signOut;
    }


    public String getCanUseUsername()
    {
      return canUseUsername;
    }

    public void setCanUseUsername(String canUseUsername)
    {
      this.canUseUsername = canUseUsername;
    }

    public String getCanUsePassword()
    {
      return canUsePassword;
    }

    public void setCanUsePassword(String canUsePassword)
    {
      this.canUsePassword = canUsePassword;
    }

    public String getGoogleCaptchaValidate()
    {
      return googleCaptchaValidate;
    }

    public void setGoogleCaptchaValidate(String googleCaptchaValidate)
    {
      this.googleCaptchaValidate = googleCaptchaValidate;
    }

    public String getCreateAccount()
    {
      return createAccount;
    }

    public void setCreateAccount(String createAccount)
    {
      this.createAccount = createAccount;
    }

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getDeleteAccount()
    {
      return deleteAccount;
    }

    public void setDeleteAccount(String deleteAccount)
    {
      this.deleteAccount = deleteAccount;
    }


    public String getGetAccountAccess()
    {
      return getAccountAccess;
    }

    public void setGetAccountAccess(String getAccountAccess)
    {
      this.getAccountAccess = getAccountAccess;
    }

    public String getPasswordChange()
    {
      return passwordChange;
    }

    public void setPasswordChange(String passwordChange)
    {
      this.passwordChange = passwordChange;
    }

    public String getPasswordResetReq()
    {
      return passwordResetReq;
    }

    public void setPasswordResetReq(String passwordResetReq)
    {
      this.passwordResetReq = passwordResetReq;
    }

    public String getPasswordResetConfirm()
    {
      return passwordResetConfirm;
    }

    public void setPasswordResetConfirm(String passwordResetConfirm)
    {
      this.passwordResetConfirm = passwordResetConfirm;
    }

    public String getPasswordReset()
    {
      return passwordReset;
    }

    public void setPasswordReset(String passwordReset)
    {
      this.passwordReset = passwordReset;
    }


    public String getGetAccountEmail()
    {
      return getAccountEmail;
    }

    public void setGetAccountEmail(String getAccountEmail)
    {
      this.getAccountEmail = getAccountEmail;
    }

    public String getEmailChange()
    {
      return emailChange;
    }

    public void setEmailChange(String emailChange)
    {
      this.emailChange = emailChange;
    }

    public String getEmailConfirmReq()
    {
      return emailConfirmReq;
    }

    public void setEmailConfirmReq(String emailConfirmReq)
    {
      this.emailConfirmReq = emailConfirmReq;
    }

    public String getEmailConfirm()
    {
      return emailConfirm;
    }

    public void setEmailConfirm(String emailConfirm)
    {
      this.emailConfirm = emailConfirm;
    }

    public String getUpdateAccountEmail()
    {
      return updateAccountEmail;
    }

    public void setUpdateAccountEmail(String updateAccountEmail)
    {
      this.updateAccountEmail = updateAccountEmail;
    }


    public String getGetAccountPhone()
    {
      return getAccountPhone;
    }

    public void setGetAccountPhone(String getAccountPhone)
    {
      this.getAccountPhone = getAccountPhone;
    }

    public String getPhoneChange()
    {
      return phoneChange;
    }

    public void setPhoneChange(String phoneChange)
    {
      this.phoneChange = phoneChange;
    }

    public String getUpdateAccountPhone()
    {
      return updateAccountPhone;
    }

    public void setUpdateAccountPhone(String updateAccountPhone)
    {
      this.updateAccountPhone = updateAccountPhone;
    }


    public String getGetAccountPersonal()
    {
      return getAccountPersonal;
    }

    public void setGetAccountPersonal(String getAccountPersonal)
    {
      this.getAccountPersonal = getAccountPersonal;
    }

    public String getNicknameChange()
    {
      return nicknameChange;
    }

    public void setNicknameChange(String nicknameChange)
    {
      this.nicknameChange = nicknameChange;
    }

    public String getUpdateAccountPersonal()
    {
      return updateAccountPersonal;
    }

    public void setUpdateAccountPersonal(String updateAccountPersonal)
    {
      this.updateAccountPersonal = updateAccountPersonal;
    }
  }

  public static class GoogleCaptcha
  {
    @NotBlank
    private String secretKey;

    @NotBlank
    private String siteVerifyUrlTemplate;

    public String getSecretKey()
    {
      return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
      this.secretKey = secretKey;
    }

    public String getSiteVerifyUrlTemplate()
    {
      return siteVerifyUrlTemplate;
    }

    public void setSiteVerifyUrlTemplate(String siteVerifyUrlTemplate)
    {
      this.siteVerifyUrlTemplate = siteVerifyUrlTemplate;
    }
  }

  public static class Frontend
  {
    private String host = "http://localhost";
    private String resourceEmailConfirm = "/email-confirm";
    private String resourcePasswordResetConfirm = "/password-reset";

    public String getHost()
    {
      return host;
    }

    public void setHost(String host)
    {
      this.host = host;
    }

    public String getResourceEmailConfirm()
    {
      return resourceEmailConfirm;
    }

    public void setResourceEmailConfirm(String resourceEmailConfirm)
    {
      this.resourceEmailConfirm = resourceEmailConfirm;
    }

    public String getResourcePasswordResetConfirm()
    {
      return resourcePasswordResetConfirm;
    }

    public void setResourcePasswordResetConfirm(String resourcePasswordResetConfirm)
    {
      this.resourcePasswordResetConfirm = resourcePasswordResetConfirm;
    }
  }

  public static class ScheduledTaskMailing
  {
    private int initialDelay = 0;

    private int collectFromDbInterval = 60_000; // 1 minute

    private int sendInterval = 30_000; // 30 seconds

    private int maxAttemptsToSendIfError = 3;

    private int timeoutForDuplicateRequest; // calculated field!!! See method: afterPropertiesSet()

    public int getInitialDelay()
    {
      return initialDelay;
    }

    public void setInitialDelay(int initialDelay)
    {
      this.initialDelay = initialDelay;
    }

    public int getCollectFromDbInterval()
    {
      return collectFromDbInterval;
    }

    public void setCollectFromDbInterval(int collectFromDbInterval)
    {
      this.collectFromDbInterval = collectFromDbInterval;
    }

    public int getSendInterval()
    {
      return sendInterval;
    }

    public void setSendInterval(int sendInterval)
    {
      this.sendInterval = sendInterval;
    }

    public int getMaxAttemptsToSendIfError()
    {
      return maxAttemptsToSendIfError;
    }

    public void setMaxAttemptsToSendIfError(int maxAttemptsToSendIfError)
    {
      this.maxAttemptsToSendIfError = maxAttemptsToSendIfError;
    }

    public int getTimeoutForDuplicateRequest()
    {
      return timeoutForDuplicateRequest;
    }

    public void setTimeoutForDuplicateRequest(int timeoutForDuplicateRequest)
    {
      this.timeoutForDuplicateRequest = timeoutForDuplicateRequest;
    }
  }

  public static class OAuth2ClientTrusted
  {
    @NotBlank
    private String id;

    @NotBlank
    private String password;

    private int tokenValiditySeconds = 60 * 60 * 24 * 20; // 20 days

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }

    public int getTokenValiditySeconds()
    {
      return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds)
    {
      this.tokenValiditySeconds = tokenValiditySeconds;
    }
  }

  public static class OAuth2ClientUntrusted
  {
    @NotBlank
    private String id;

    @NotBlank
    private String password;

    private int tokenValiditySeconds = 60 * 3; // 3 minutes

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }

    public int getTokenValiditySeconds()
    {
      return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds)
    {
      this.tokenValiditySeconds = tokenValiditySeconds;
    }
  }

  public String getApi()
  {
    return api;
  }

  public void setApi(String api)
  {
    this.api = api;
  }

  public Resource getResource()
  {
    return resource;
  }

  public void setResource(Resource resource)
  {
    this.resource = resource;
  }

  public GoogleCaptcha getGoogleCaptcha()
  {
    return googleCaptcha;
  }

  public void setGoogleCaptcha(GoogleCaptcha googleCaptcha)
  {
    this.googleCaptcha = googleCaptcha;
  }

  public Frontend getFrontend()
  {
    return frontend;
  }

  public void setFrontend(Frontend frontend)
  {
    this.frontend = frontend;
  }

  public ScheduledTaskMailing getScheduledTaskMailing()
  {
    return scheduledTaskMailing;
  }

  public void setScheduledTaskMailing(ScheduledTaskMailing scheduledTaskMailing)
  {
    this.scheduledTaskMailing = scheduledTaskMailing;
  }

  public OAuth2ClientTrusted getOauth2ClientTrusted()
  {
    return oauth2ClientTrusted;
  }

  public void setOauth2ClientTrusted(OAuth2ClientTrusted oauth2ClientTrusted)
  {
    this.oauth2ClientTrusted = oauth2ClientTrusted;
  }

  public OAuth2ClientUntrusted getOauth2ClientUntrusted()
  {
    return oauth2ClientUntrusted;
  }

  public void setOauth2ClientUntrusted(OAuth2ClientUntrusted oauth2ClientUntrusted)
  {
    this.oauth2ClientUntrusted = oauth2ClientUntrusted;
  }

  @Override
  public String toString()
  {
    return String.format("%n%n" +
                    "-====================================================-%n" +
                    "|                  ::[Nevis server]::                |%n" +
                    "|----------------------------------------------------|%n" +
                    "| API                               %s%n" +
                    "|                                                     %n" +
                    "| Scheduled Task - Mailing:                           %n" +
                    "|    initial delay                  %s%n" +
                    "|    collect from DB interval       %s%n" +
                    "|    send interval                  %s%n" +
                    "|    max attempts to send if error  %s%n" +
                    "|    timeout for duplicate request  %s%n" +
                    "|                                                     %n" +
                    "| RESOURCES                                           %n" +
                    "|                                                     %n" +
                    "|   Auth:                                             %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|   Account.Common:                                   %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|   Account.Access:                                   %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|   Account.Email:                                    %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|   Account.Phone:                                    %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|   Account.Personal:                                 %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "| Frontend                                            %n" +
                    "|    host                   %s%n" +
                    "|    resources for:                                   %n" +
                    "|      /password-reset-confirm  %s%n" +
                    "|      /email-confirm           %s%n" +
                    "|_____________________________________________________%n",
            api,
            formatMillisecondsToReadableString(scheduledTaskMailing.initialDelay),
            formatMillisecondsToReadableString(scheduledTaskMailing.collectFromDbInterval),
            formatMillisecondsToReadableString(scheduledTaskMailing.sendInterval),
            scheduledTaskMailing.maxAttemptsToSendIfError,
            formatMillisecondsToReadableString(scheduledTaskMailing.timeoutForDuplicateRequest),

            // Auth
            resource.signIn,
            resource.signOut,

            // Account.Common
            resource.canUseUsername,
            resource.canUsePassword,
            resource.googleCaptchaValidate,
            resource.createAccount,
            resource.id,
            resource.deleteAccount,

            // Account.Access
            resource.getAccountAccess,
            resource.passwordChange,
            resource.passwordResetReq,
            resource.passwordResetConfirm,
            resource.passwordReset,

            // Account.Email
            resource.getAccountEmail,
            resource.emailConfirmReq,
            resource.emailConfirm,
            resource.emailChange,
            resource.updateAccountEmail,

            // Account.Phone
            resource.getAccountEmail,
            resource.phoneChange,
            resource.updateAccountPhone,

            // Account.Personal
            resource.getAccountPersonal,
            resource.nicknameChange,
            resource.updateAccountPersonal,

            // Frontend
            frontend.host,
            frontend.resourcePasswordResetConfirm,
            frontend.resourceEmailConfirm
    );
  }
}
