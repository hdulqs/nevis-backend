package dwfe.config;

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

import static dwfe.util.DwfeUtil.formatMillisecondsToReadableString;

@Validated
@Configuration
@ConfigurationProperties(prefix = "dwfe")
public class DwfeConfigProperties implements InitializingBean
{
  private final static Logger log = LoggerFactory.getLogger(DwfeConfigProperties.class);

  @NotBlank
  private String api;
  private String apiRoot;

  @NotNull
  private ScheduledTaskMailing scheduledTaskMailing;

  private Resource resource = new Resource();

  private Frontend frontend = new Frontend();

  private Captcha captcha;

  private final Environment env;

  @Autowired
  public DwfeConfigProperties(Environment env)
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

    log.info(toString());
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

  public static class Resource
  {
    // Third-party
    private String googleCaptchaValidate = "/google-captcha-validate";

    public String getGoogleCaptchaValidate()
    {
      return googleCaptchaValidate;
    }

    public void setGoogleCaptchaValidate(String googleCaptchaValidate)
    {
      this.googleCaptchaValidate = googleCaptchaValidate;
    }
  }

  public static class Frontend
  {
    private String host = "http://localhost";

    public String getHost()
    {
      return host;
    }

    public void setHost(String host)
    {
      this.host = host;
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

  public ScheduledTaskMailing getScheduledTaskMailing()
  {
    return scheduledTaskMailing;
  }

  public void setScheduledTaskMailing(ScheduledTaskMailing scheduledTaskMailing)
  {
    this.scheduledTaskMailing = scheduledTaskMailing;
  }

  public Resource getResource()
  {
    return resource;
  }

  public void setResource(Resource resource)
  {
    this.resource = resource;
  }

  public Frontend getFrontend()
  {
    return frontend;
  }

  public void setFrontend(Frontend frontend)
  {
    this.frontend = frontend;
  }

  public Captcha getCaptcha()
  {
    return captcha;
  }

  public void setCaptcha(Captcha captcha)
  {
    this.captcha = captcha;
  }

  @Override
  public String toString()
  {
    return String.format("%n%n" +
                    "-====================================================-%n" +
                    "|                  Do|While|For|Each                 |%n" +
                    "|----------------------------------------------------|%n" +
                    "|                                                     %n" +
                    "| API Root                          %s%n" +
                    "|                                                     %n" +
                    "| API Resources                                       %n" +
                    "|                                                     %n" +
                    "|   Third-party:                                      %n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "|                                                     %n" +
                    "| FRONTEND                                            %n" +
                    "|                                                     %n" +
                    "|   host                            %s%n" +
                    "|                                                     %n" +
                    "|                                                     %n" +
                    "| Is Third-party initialized?                         %n" +
                    "|   Google Captcha                  %s%n" +
                    "|                                                     %n" +
                    "|                                                     %n" +
                    "| Scheduled Task - Mailing:                           %n" +
                    "|   initial delay                   %s%n" +
                    "|   collect from DB interval        %s%n" +
                    "|   send interval                   %s%n" +
                    "|   max attempts to send if error   %s%n" +
                    "|   timeout for duplicate request   %s%n" +
                    "|_____________________________________________________%n%n",
            apiRoot,

            // Third-party
            resource.googleCaptchaValidate,

            // Frontend
            frontend.host,

            // Is Third-party initialized?
            captcha.googleSecretKey != null,

            // Scheduled Task - Mailing
            formatMillisecondsToReadableString(scheduledTaskMailing.initialDelay),
            formatMillisecondsToReadableString(scheduledTaskMailing.collectFromDbInterval),
            formatMillisecondsToReadableString(scheduledTaskMailing.sendInterval),
            scheduledTaskMailing.maxAttemptsToSendIfError,
            formatMillisecondsToReadableString(scheduledTaskMailing.timeoutForDuplicateRequest)
    );
  }
}
