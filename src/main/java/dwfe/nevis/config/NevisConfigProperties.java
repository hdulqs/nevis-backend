package dwfe.nevis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

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
  private String apiRoot;

  private Resource resource = new Resource();

  private Captcha captcha;

  private ThirdPartyAuth thirdPartyAuth;

  @NotNull
  private Frontend frontend = new Frontend();

  @NotNull
  private ScheduledTaskMailing scheduledTaskMailing;

  @NotNull
  private OAuth2ClientUntrusted oauth2ClientUntrusted;
  @NotNull
  private OAuth2ClientTrusted oauth2ClientTrusted;
  @NotNull
  private OAuth2ClientUnlimited oAuth2ClientUnlimited;

  private final Environment env;

  @Autowired
  public NevisConfigProperties(Environment env)
  {
    this.env = env;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    if (scheduledTaskMailing.getTimeoutForDuplicateRequest() <= 0)
      scheduledTaskMailing.setTimeoutForDuplicateRequest(
              scheduledTaskMailing.getSendInterval() * scheduledTaskMailing.getMaxAttemptsToSendIfError());

    var address = env.getProperty("server.address");
    var port = env.getProperty("server.port");
    apiRoot = "http://" + address + ":" + port + api;

    log.warn(toString());
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
    private String thirdPartyAuth = "/third-party-auth";

    // Account.Access
    private String getAccountAccess = "/get-account-access";
    private String passwordChange = "/password-change";
    private String passwordResetReq = "/password-reset-req";
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

    public String getThirdPartyAuth()
    {
      return thirdPartyAuth;
    }

    public void setThirdPartyAuth(String thirdPartyAuth)
    {
      this.thirdPartyAuth = thirdPartyAuth;
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

  public static class Captcha
  {
    @NotBlank
    private String googleSecretKey;

    public String getGoogleSecretKey()
    {
      return googleSecretKey;
    }

    public void setGoogleSecretKey(String googleSecretKey)
    {
      this.googleSecretKey = googleSecretKey;
    }
  }

  public static class ThirdPartyAuth
  {
    private String googleClientId;

    private String facebookAppId;
    private String facebookAppSecret;

    public String getGoogleClientId()
    {
      return googleClientId;
    }

    public void setGoogleClientId(String googleClientId)
    {
      this.googleClientId = googleClientId;
    }

    public String getFacebookAppId()
    {
      return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId)
    {
      this.facebookAppId = facebookAppId;
    }

    public String getFacebookAppSecret()
    {
      return facebookAppSecret;
    }

    public void setFacebookAppSecret(String facebookAppSecret)
    {
      this.facebookAppSecret = facebookAppSecret;
    }
  }

  public static class Frontend
  {
    private String host = "http://localhost";
    private String resourceEmailConfirm = "/email-confirm";
    private String resourcePasswordReset = "/password-reset";
    private String resourceAccount = "/account";

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

    public String getResourcePasswordReset()
    {
      return resourcePasswordReset;
    }

    public void setResourcePasswordReset(String resourcePasswordReset)
    {
      this.resourcePasswordReset = resourcePasswordReset;
    }

    public String getResourceAccount()
    {
      return resourceAccount;
    }

    public void setResourceAccount(String resourceAccount)
    {
      this.resourceAccount = resourceAccount;
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

  public static class OAuth2ClientUntrusted
  {
    @NotBlank
    private String id;
    @NotBlank
    private String password;

    @NotNull
    @PositiveOrZero
    private Integer accessTokenValiditySeconds = 60 * 3; // 3 min.
    @NotNull
    @PositiveOrZero
    private Integer refreshTokenValiditySeconds = 1;     // 1 sec.

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

    public Integer getAccessTokenValiditySeconds()
    {
      return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds)
    {
      this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public Integer getRefreshTokenValiditySeconds()
    {
      return refreshTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds)
    {
      this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }
  }

  public static class OAuth2ClientTrusted
  {
    @NotBlank
    private String id;
    @NotBlank
    private String password;

    @NotNull
    @PositiveOrZero
    private Integer accessTokenValiditySeconds = 60 * 60 * 24 * 20;   // 20 days
    @NotNull
    @PositiveOrZero
    private Integer refreshTokenValiditySeconds = 60 * 60 * 24 * 340; // 340 days

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

    public Integer getAccessTokenValiditySeconds()
    {
      return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds)
    {
      this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public Integer getRefreshTokenValiditySeconds()
    {
      return refreshTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds)
    {
      this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }
  }

  public static class OAuth2ClientUnlimited
  {
    @NotBlank
    private String id;
    @NotBlank
    private String password;

    @NotNull
    @PositiveOrZero
    private Integer accessTokenValiditySeconds = 0;  // unlimite

    @NotNull
    @PositiveOrZero
    private Integer refreshTokenValiditySeconds = 1; // 1 sec.

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

    public Integer getAccessTokenValiditySeconds()
    {
      return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds)
    {
      this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public Integer getRefreshTokenValiditySeconds()
    {
      return refreshTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds)
    {
      this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
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

  public String getApiRoot()
  {
    return apiRoot;
  }

  public void setApiRoot(String apiRoot)
  {
    this.apiRoot = apiRoot;
  }

  public Resource getResource()
  {
    return resource;
  }

  public void setResource(Resource resource)
  {
    this.resource = resource;
  }

  public Captcha getCaptcha()
  {
    return captcha;
  }

  public void setCaptcha(Captcha captcha)
  {
    this.captcha = captcha;
  }

  public ThirdPartyAuth getThirdPartyAuth()
  {
    return thirdPartyAuth;
  }

  public void setThirdPartyAuth(ThirdPartyAuth thirdPartyAuth)
  {
    this.thirdPartyAuth = thirdPartyAuth;
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

  public OAuth2ClientUntrusted getOauth2ClientUntrusted()
  {
    return oauth2ClientUntrusted;
  }

  public void setOauth2ClientUntrusted(OAuth2ClientUntrusted oauth2ClientUntrusted)
  {
    this.oauth2ClientUntrusted = oauth2ClientUntrusted;
  }

  public OAuth2ClientTrusted getOauth2ClientTrusted()
  {
    return oauth2ClientTrusted;
  }

  public void setOauth2ClientTrusted(OAuth2ClientTrusted oauth2ClientTrusted)
  {
    this.oauth2ClientTrusted = oauth2ClientTrusted;
  }

  public OAuth2ClientUnlimited getoAuth2ClientUnlimited()
  {
    return oAuth2ClientUnlimited;
  }

  public void setoAuth2ClientUnlimited(OAuth2ClientUnlimited oAuth2ClientUnlimited)
  {
    this.oAuth2ClientUnlimited = oAuth2ClientUnlimited;
  }

  @Override
  public String toString()
  {
    return String.format("%n%n" +
                    "-====================================================-%n" +
                    "|                     ::[Nevis]::                    |%n" +
                    "|----------------------------------------------------|%n" +
                    "|                                                     %n" +
                    "| API Root                          %s%n" +
                    "|                                                     %n" +
                    "|                                                     %n" +
                    "| OAuth2 Clients tokens validity (seconds):           %n" +
                    "|   Untrusted                                         %n" +
                    "|     access_token                  %s%n" +
                    "|     refresh_token                 %s%n" +
                    "|   Trusted                                           %n" +
                    "|     access_token                  %s%n" +
                    "|     refresh_token                 %s%n" +
                    "|   Unlimited                                         %n" +
                    "|     access_token                  %s%n" +
                    "|     refresh_token                 %s%n" +
                    "|                                                     %n" +
                    "|                                                     %n" +
                    "| Scheduled Task - Mailing:                           %n" +
                    "|   initial delay                   %s%n" +
                    "|   collect from DB interval        %s%n" +
                    "|   send interval                   %s%n" +
                    "|   max attempts to send if error   %s%n" +
                    "|   timeout for duplicate request   %s%n" +
                    "|                                                     %n" +
                    "|                                                     %n" +
                    "| Is Third-party initialized?                         %n" +
                    "|   Google Captcha                  %s%n" +
                    "|   Google Sign                     %s%n" +
                    "|   Facebook Sign                   %s%n" +
                    "|                                                     %n" +
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
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|   Account.Access:                                   %n" +
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
                    "|                                                     %n" +
                    "| FRONTEND                                            %n" +
                    "|                                                     %n" +
                    "|   host                       %s%n" +
                    "|   resources for:                                    %n" +
                    "|     password rest            %s%n" +
                    "|     email confirm            %s%n" +
                    "|     account                  %s%n" +
                    "|_____________________________________________________%n%n",
            apiRoot,

            // OAuth2 Clients tokens validity
            oauth2ClientUntrusted.accessTokenValiditySeconds,
            oauth2ClientUntrusted.refreshTokenValiditySeconds,
            oauth2ClientTrusted.accessTokenValiditySeconds,
            oauth2ClientTrusted.refreshTokenValiditySeconds,
            oAuth2ClientUnlimited.accessTokenValiditySeconds,
            oAuth2ClientUnlimited.refreshTokenValiditySeconds,

            // Scheduled Task - Mailing
            formatMillisecondsToReadableString(scheduledTaskMailing.initialDelay),
            formatMillisecondsToReadableString(scheduledTaskMailing.collectFromDbInterval),
            formatMillisecondsToReadableString(scheduledTaskMailing.sendInterval),
            scheduledTaskMailing.maxAttemptsToSendIfError,
            formatMillisecondsToReadableString(scheduledTaskMailing.timeoutForDuplicateRequest),

            // Is Third-party initialized?
            captcha.googleSecretKey != null,
            thirdPartyAuth.googleClientId != null,
            thirdPartyAuth.facebookAppId != null && thirdPartyAuth.facebookAppSecret != null,

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
            resource.thirdPartyAuth,

            // Account.Access
            resource.getAccountAccess,
            resource.passwordChange,
            resource.passwordResetReq,
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
            frontend.resourcePasswordReset,
            frontend.resourceEmailConfirm,
            frontend.resourceAccount
    );
  }
}
