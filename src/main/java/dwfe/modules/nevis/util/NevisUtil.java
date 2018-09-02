package dwfe.modules.nevis.util;

import dwfe.modules.nevis.config.NevisConfigProperties;
import dwfe.modules.nevis.db.account.access.NevisAccountUsernameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dwfe.util.DwfeUtil.isExceededMaxCheckOk;

@Component
@PropertySource("classpath:application.properties")
public class NevisUtil
{
  private final NevisConfigProperties prop;

  @Autowired
  public NevisUtil(NevisConfigProperties prop)
  {
    this.prop = prop;
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

  public static NevisAccountUsernameType getUsernameTypeFromStr(String value)
  {
    NevisAccountUsernameType usernameType = null;

    if (value != null && !value.isEmpty())
      try
      {
        usernameType = NevisAccountUsernameType.valueOf(value);
      }
      catch (IllegalArgumentException ignored)
      {
      }
    return usernameType;
  }
}
