package dwfe.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dwfe.config.DwfeConfigProperties;
import dwfe.db.mailing.DwfeMailingService;
import dwfe.db.mailing.DwfeMailingType;
import dwfe.db.other.DwfeModule;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DwfeUtil
{
  private final RestTemplate restTemplate;
  private final RestTemplateBuilder restTemplateBuilder;

  private final DwfeConfigProperties propDwfe;
  private final DwfeMailingService mailingService;

  @Autowired
  public DwfeUtil(RestTemplateBuilder restTemplateBuilder, DwfeConfigProperties propDwfe, DwfeMailingService mailingService)
  {
    this.restTemplate = restTemplateBuilder.build();
    this.restTemplateBuilder = restTemplateBuilder;

    this.propDwfe = propDwfe;
    this.mailingService = mailingService;
  }

  //-------------------------------------------------------
  // Random
  //

  public static String getRandomAlphaNumeric(int requiredLength)
  {
    return RandomStringUtils.randomAlphanumeric(requiredLength);

    // var result = getRandomStrBase64(requiredLength);
    // return result.replaceAll("[^a-zA-Z0-9]", "");
  }

  public static String getRandomPrintableASCII(int requiredLength)
  {
    return new RandomStringGenerator.Builder()
            .withinRange('!', '~') // except Space
            .build()               // == https://en.wikipedia.org/wiki/ASCII#Printable_characters
            .generate(requiredLength);
  }

  public static String getRandomStrBase36(int requiredLength)
  {
    // Disadvantage - the result produces without uppercase alphabetic characters:
    //
    // 1epmcp0gayc51k1s2ay5g6ogq2vvcy6
    // hwsr2qa2y5muoilgcx854femz87hbnp
    // k82c6djt7mdp78oclqcstd0d7sxyq7k
    return new BigInteger(requiredLength * 5, new SecureRandom()).toString(36);
  }

  public static String getRandomStrBase64(int requiredLength)
  {
    // Common Disadvantage is that some random are not very beautiful :)
    //
    //
    // (requiredLength + 3) and new String(..., 2,...)
    // because first letter repeated:
    //    AfhFTjpSSg==
    //    AfhFTjpSSg==
    //    Aj3ibDty2g==
    //    AqXQoW3d1w==
    //    A42HUbmWPw==
    //    At0DXvTA/Q==
    //
    // new String(..., ..., requiredLength)
    // because encoder adds postfix "=="
    //
    // SUMMARY:
    // X requiredLength ==

    var target = new BigInteger((requiredLength + 3) * 5, new SecureRandom()).toString();

    // because target length can be less than requiredLength
    var realLength = target.length() > requiredLength ? requiredLength : target.length();

    var bytes = target.getBytes();
    return new String(Base64.getEncoder().encode(bytes), 2, realLength);
  }


  //-------------------------------------------------------
  // JSON
  //

  public static String getJsonFieldFromObj(String name, Object value)
  {
    return value == null
            ? "\"" + name + "\": null"
            : "\"" + name + "\": \"" + value + "\"";
  }

  public static String listToJson(List<String> list)
  {
    return "{" + list.stream().collect(Collectors.joining(",")) + "}";
  }

  public static String getJsonFromObj(Object value)
  {
    var result = "{}";
    if (value != null)
      try
      {
        var mapper = new ObjectMapper();
        result = mapper.writeValueAsString(value);
      }
      catch (JsonProcessingException e)
      {
        e.printStackTrace();
      }
    return result;
  }

  public static Object getPropValueFromJson(String prop, String json)
  {
    return JsonParserFactory.getJsonParser().parseMap(json).get(prop);
  }

  public static Map<String, Object> getMapFromJson(String json)
  {
    return JsonParserFactory.getJsonParser().parseMap(json);
  }


  //-------------------------------------------------------
  // Date and Time
  //

  public static String formatMillisecondsToReadableString(long millis)
  {
    return String.format("%02d min, %02d sec",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    ); // https://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java#625624
  }

  public static String formatDateTimeToUTCstring(LocalDateTime localDateTime)
  {
    // ISO dates can be written with added hours, minutes, and seconds (YYYY-MM-DDTHH:MM:SSZ):
    //   "2015-03-25T12:00:00Z"
    // Date and time is separated with a capital T.
    // UTC time is defined with a capital letter Z.
    //
    // https://docs.oracle.com/javase/10/docs/api/java/time/format/DateTimeFormatter.html#predefined
    // I can't use ISO_INSTANT formmatter because LocalDateTime not contains info about time zone, for this reason:
    return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

    // If you want to modify the time relative to UTC, remove the Z and add +HH:MM or -HH:MM instead:
    //   "2015-03-25T12:00:00-06:30"
    // But I strongly do not recommend doing this, otherwise you need to consider changing the time zone
    // in other places of this project: https://github.com/dowhileforeach/authtion#date-and-time
    // and don't forget about the time zone of MySQL
  }


  //-------------------------------------------------------
  // CHECK
  //

  public static boolean isDefaultPreCheckOk(String value, String name, List<String> errorCodes)
  {
    return isNotNullPreCheckOk(value, name, errorCodes)
            && isEmptyPreCheckOk(value, name, errorCodes);
  }

  public static boolean isNotNullPreCheckOk(Object value, String name, List<String> errorCodes)
  {
    if (value == null)
      errorCodes.add("missing-" + name);
    return errorCodes.size() == 0;
  }

  public static boolean isEmptyPreCheckOk(String value, String name, List<String> errorCodes)
  {
    if (value.isEmpty())
      errorCodes.add("empty-" + name);
    return errorCodes.size() == 0;
  }

  public static boolean isExceededMaxCheckOk(String value, String name, int max, List<String> errorCodes)
  {
    if (value.length() > max)
      errorCodes.add(String.format("exceeded-max%s-%s-length", max, name));
    return errorCodes.size() == 0;
  }

  public static boolean isRangeMinMaxCheckOk(String value, String name, int min, int max, List<String> errorCodes)
  {
    if (value.length() < min || value.length() > max)
      errorCodes.add(String.format("out-of-range-min%s-max%s-%s-length", min, max, name));
    return errorCodes.size() == 0;
  }

  public boolean isAllowedNewRequestForMailing(DwfeModule module, DwfeMailingType type, String email, List<String> errorCodes)
  {
    var lastPending = mailingService.findLastByModuleAndTypeAndEmail(module, type, email);
    if (lastPending.isPresent())
    {
      var whenNewIsAllowed = lastPending.get()
              .getCreatedOn()
              .plus(propDwfe.getScheduledTaskMailing().getTimeoutForDuplicateRequest(), ChronoUnit.MILLIS);

      if (whenNewIsAllowed.isAfter(LocalDateTime.now()))
        errorCodes.add("delay-between-duplicate-requests");
    }
    return errorCodes.size() == 0;
  }


  //-------------------------------------------------------
  // EXCHANGE
  //

  public static String getResponse(List<String> errorCodes)
  {
    if (errorCodes.size() == 0)
      return "{\"success\": true}";
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  public static String getResponse(List<String> errorCodes, String data)
  {
    if (errorCodes.size() == 0)
      return getResponseSuccessWithData(data);
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  public static String getResponse(List<String> errorCodes, Map<String, Object> data)
  {
    if (errorCodes.size() == 0)
      return getResponseSuccessWithData(getJsonFromObj(data));
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  public static String getResponseSuccessWithData(String data)
  {
    return String.format("{\"success\": true, \"data\": %s}", data);
  }

  public static String getResponseWithErrorCodes(List<String> errorCodes)
  {
    return String.format("{\"success\": false, \"error-codes\": %s}", getJsonFromObj(errorCodes));
  }

  public Map<String, Object> exchangeWrap(String url, HttpMethod method, long secondsToWait, String errName, List<String> errorCodes)
  {
    Map<String, Object> result = null;
    try
    {
      var exchange = new FutureTask<>(() -> restTemplate.exchange(url, method, null, String.class));
      new Thread(exchange).start();
      var response = exchange.get(secondsToWait, TimeUnit.SECONDS);
      if (response.getStatusCodeValue() == 200)
        result = getMapFromJson(response.getBody());
      else
        errorCodes.add(errName + "-error-exchange");
    }
    catch (Throwable e)
    {
      errorCodes.add(errName + "-error-connection");
    }
    return result;
  }


  //-------------------------------------------------------
  // OTHER
  //

  public static String cutStr(String value, int maxLength)
  {
    if (value == null)
      return null;
    else if (value.length() > maxLength)
      return value.substring(0, maxLength);
    else
      return value;
  }

  public static String strToUpperCase(String value)
  {
    return value == null ? null : value.toUpperCase();
  }

  public static String nullableValueToStrResp(String field, Object value)
  {
    return value == null
            ? "\"" + field + "\":null"
            : "\"" + field + "\":\"" + value + "\"";
  }
}
