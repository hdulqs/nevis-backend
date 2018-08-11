package dwfe.nevis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dwfe.nevis.config.NevisConfigProperties;
import dwfe.nevis.db.account.access.NevisAccountUsernameType;
import dwfe.nevis.db.mailing.NevisMailingService;
import dwfe.nevis.db.mailing.NevisMailingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:application.properties")
public class NevisUtil
{
  private final NevisConfigProperties prop;
  private final NevisMailingService mailingService;

  @Autowired
  public NevisUtil(NevisConfigProperties prop, NevisMailingService mailingService)
  {
    this.prop = prop;
    this.mailingService = mailingService;
  }


  //-------------------------------------------------------
  // Email
  //

  // http://emailregex.com/
  // RFC 5322: http://www.ietf.org/rfc/rfc5322.txt
  private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);

  public static boolean isEmailValid(String email, String name, List<String> errorCodes)
  {
    if (!EMAIL_PATTERN.matcher(email).matches())
      errorCodes.add("invalid-" + name);
    return errorCodes.size() == 0;
  }

  public static boolean standardEmailCheck(String email, String name, List<String> errorCodes)
  {
    return isExceededMaxCheckOk(email, name, 100, errorCodes)
            && isEmailValid(email, name, errorCodes);
  }


  //-------------------------------------------------------
  // ID
  //

  // 1. id is long type        => max 19 char.
  // 2. AUTO_INCREMENT = 1000  => min 4 char.
  private static final Pattern ID_PATTERN = Pattern.compile("[0-9]{4,19}");

  public static boolean isIdValid(String id, List<String> errorCodes)
  {
    if (!ID_PATTERN.matcher(id).matches())
      errorCodes.add("id-must-be-a-positive-number");
    else
      try
      {
        Long.parseLong(id);
      }
      catch (NumberFormatException e)
      {
        errorCodes.add("id-must-be-of-type-long");
      }
    return errorCodes.size() == 0;
  }


  //-------------------------------------------------------
  // Authorities
  //

  public static List<String> getAuthorities(Collection<? extends GrantedAuthority> authorities, boolean extra)
  {
    if (extra)
      return authorities.stream()
              .map(a -> "\"" + a.getAuthority() + "\"")
              .collect(Collectors.toList());
    else
      return authorities.stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());
  }


  //-------------------------------------------------------
  // Password
  //

  // org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
  private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a\\$10\\$[./0-9A-Za-z]{53}");

  public static boolean isStringBcrypted(String string)
  {
    return BCRYPT_PATTERN.matcher(string).matches();
  }

  public static boolean matchPassword(String rawPassword, String rawEncodedPassword)
  {
    var encodedPassword = rawEncodedPassword.replace("{bcrypt}", "");
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }

  public static String preparePasswordForDB(String password)
  {
    if (isStringBcrypted(password))
      return "{bcrypt}" + password;
    else
      return "{bcrypt}" + new BCryptPasswordEncoder(10).encode(password);
  }


  //-------------------------------------------------------
  // Random
  //

  public static String getRandomStrBase36(int requiredLength)
  {
    return new BigInteger(requiredLength * 5, new SecureRandom()).toString(36);
  }

  public static String getRandomStrBase64(int requiredLength)
  {
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

  public static String getRandomStrAlphaDigit(int requiredLength)
  {
    var result = getRandomStrBase64(requiredLength);
    return result.replaceAll("[^a-zA-Z0-9]", "");
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
  // API
  //

  public String prepareSignInUrl(String username, String password, NevisAccountUsernameType usernameType)
  {
    return String.format(prop.getApiRoot() + prop.getResource().getSignIn()
                    + "?grant_type=password&username=%1$s&password=%2$s%3$s",
            URLEncoder.encode(username, StandardCharsets.UTF_8),
            URLEncoder.encode(password, StandardCharsets.UTF_8),
            usernameType == null ? "" : "&usernameType=" + usernameType
    );
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


  public static NevisAccountUsernameType getUsernameTypeFromStr(String value)
  {
    NevisAccountUsernameType result = null;

    if (value != null && !value.isEmpty())
      try
      {
        result = NevisAccountUsernameType.valueOf(value);
      }
      catch (IllegalArgumentException ignored)
      {
      }
    return result;
  }

  public static String nullableValueToStrResp(String field, Object value)
  {
    return value == null
            ? "\"" + field + "\":null"
            : "\"" + field + "\":\"" + value + "\"";
  }

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


  public boolean isAllowedNewRequestForMailing(NevisMailingType type, String email, List<String> errorCodes)
  {
    var lastPending = mailingService.findLastByTypeAndEmail(type, email);
    if (lastPending.isPresent())
    {
      var whenNewIsAllowed = lastPending.get()
              .getCreatedOn()
              .plus(prop.getScheduledTaskMailing().getTimeoutForDuplicateRequest(), ChronoUnit.MILLIS);

      if (whenNewIsAllowed.isAfter(LocalDateTime.now()))
        errorCodes.add("delay-between-duplicate-requests");
    }
    return errorCodes.size() == 0;
  }
}
