package dwfe.schedule;

import dwfe.config.DwfeConfigProperties;
import dwfe.db.mailing.DwfeMailing;
import dwfe.db.mailing.DwfeMailingService;
import dwfe.db.mailing.DwfeMailingType;
import dwfe.db.other.DwfeModule;
import dwfe.modules.nevis.config.NevisConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static dwfe.db.mailing.DwfeMailingType.*;
import static dwfe.db.other.DwfeModule.NEVIS;

@Component
@PropertySource("classpath:application.properties")
public class DwfeScheduler
{
  private final static Logger log = LoggerFactory.getLogger(DwfeScheduler.class);

  private final DwfeConfigProperties propDwfe;
  private final NevisConfigProperties propNevis;
  private final DwfeMailingService mailingService;
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine; // Thymeleaf

  private static final ConcurrentSkipListSet<DwfeMailing> MAILING_POOL = new ConcurrentSkipListSet<>();
  private final int maxAttemptsMailingIfError;
  private final String sendFrom;

  @Autowired
  public DwfeScheduler(Environment env, DwfeConfigProperties propDwfe, NevisConfigProperties propNevis, DwfeMailingService mailingService, JavaMailSender mailSender, TemplateEngine templateEngine)
  {
    this.propDwfe = propDwfe;
    this.propNevis = propNevis;
    this.mailingService = mailingService;
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;

    this.maxAttemptsMailingIfError = propDwfe.getScheduledTaskMailing().getMaxAttemptsToSendIfError();
    this.sendFrom = env.getProperty("spring.mail.username");
  }


  @Scheduled(
          initialDelayString = "#{dwfeConfigProperties.scheduledTaskMailing.initialDelay}",
          fixedRateString = "#{dwfeConfigProperties.scheduledTaskMailing.collectFromDbInterval}")
  public void collectMailingTasksFromDatabase()
  {
    MAILING_POOL.addAll(mailingService.getNewJob());
    log.debug("mailing [{}] collected from DB", MAILING_POOL.size());
  }


  @Scheduled(
          initialDelayString = "#{dwfeConfigProperties.scheduledTaskMailing.initialDelay}",
          fixedDelayString = "#{dwfeConfigProperties.scheduledTaskMailing.sendInterval}")
  public void sendingMail()
  {
    log.debug("mailing [{}] before sending", MAILING_POOL.size());
    final var toDataBase = new ArrayList<DwfeMailing>();
    MAILING_POOL.forEach(next -> {
      var type = next.getType();
      var email = next.getEmail();
      var module = next.getModule();
      var data = next.getData();
      var subjectMessage = getSubjectMessage(type, module, data);
      try
      {
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
          var helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO);
          helper.setFrom(sendFrom);
          helper.setTo(email);
          helper.setSubject(subjectMessage.get("subject"));
          helper.setText(subjectMessage.get("message"), true);
          //mimeMessage.addHeader("Content-Transfer-Encoding", "base64"); // to auto encode message to base64
          mimeMessage.addHeader("Content-Transfer-Encoding", "quoted-printable");
        };
        mailSender.send(mimeMessagePreparator);
        next.setSent(true);
        clearMailing(type, next);
        toDataBase.add(next);
        log.debug("mailing <{}> successfully sent", email);
      }
      catch (Throwable e)
      { // an error may occur, but the email will still be sent,
        // thus it is normal to send several identical emails

        next.setCauseOfLastFailure(e.toString());

        if (next.getAttempt().incrementAndGet() > maxAttemptsMailingIfError)
        {
          next.setMaxAttemptsReached(true);
          clearMailing(type, next); // but all of a sudden the letter was sent
          toDataBase.add(next);
          log.debug("mailing <{}> last fail sending: {}", email, next.getCauseOfLastFailure());
        }
        else log.debug("mailing <{}> go to attempt[{}] after fail: {}", email, next.getAttempt().get(), next.getCauseOfLastFailure());
      }
    });

    if (toDataBase.size() > 0)
    {
      mailingService.saveAll(toDataBase);
      MAILING_POOL.removeAll(toDataBase);
      log.debug("mailing [{}] stored to DB", toDataBase.size());
      toDataBase.clear();
    }
  }

  private void clearMailing(DwfeMailingType type, DwfeMailing mailing)
  {
    // if not confirmation
    if (!(EMAIL_CONFIRM.equals(type) || PASSWORD_RESET_CONFIRM.equals(type)))
      mailing.clear();
  }

  private Map<String, String> getSubjectMessage(DwfeMailingType type, DwfeModule module, String data)
  {
    var result = new HashMap<String, String>();
    var subjKey = "subject";
    var messageKey = "message";
    var dataKey = "data";
    var context = new Context();
    var frontendHost = propDwfe.getFrontend().getHost();

    if (WELCOME_ONLY.equals(type))
    {
      result.put(subjKey, "Welcome");
      if (module == NEVIS)
      {
        context.setVariable("account_link", frontendHost + propNevis.getFrontend().getResourceAccount());
      }
    }
    else if (WELCOME_PASSWORD.equals(type))
    {
      result.put(subjKey, "Welcome");
      if (module == NEVIS)
      {
        context.setVariable(dataKey, data);
        context.setVariable("account_link", frontendHost + propNevis.getFrontend().getResourceAccount());
      }
    }
    else if (EMAIL_CONFIRM.equals(type))
    {
      result.put(subjKey, "Email confirm");
      if (module == NEVIS)
      {
        var resourceConfirmEmail = propNevis.getFrontend().getResourceEmailConfirm();
        context.setVariable(dataKey, frontendHost + resourceConfirmEmail + "/" + data);
      }
    }
    else if (PASSWORD_WAS_CHANGED.equals(type))
    {
      result.put(subjKey, "Password has been changed");
    }
    else if (PASSWORD_RESET_CONFIRM.equals(type))
    {
      result.put(subjKey, "Password reset");
      if (module == NEVIS)
      {
        var resourceConfirmResetPass = propNevis.getFrontend().getResourcePasswordReset();
        context.setVariable(dataKey, frontendHost + resourceConfirmResetPass + "/" + data);
      }
    }
    result.put(messageKey, templateEngine.process(module + "_mailing_" + type, context));
    return result;
  }
}
