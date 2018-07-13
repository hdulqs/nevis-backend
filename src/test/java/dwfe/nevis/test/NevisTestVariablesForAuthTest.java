package dwfe.nevis.test;

import dwfe.nevis.config.NevisConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import static dwfe.nevis.test.NevisTestAuthorityLevel.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Configuration
public class NevisTestVariablesForAuthTest
{
  @Autowired
  private NevisConfigProperties prop;

  //-------------------------------------------------------
  // RESOURCES
  //

  public Map<String, Map<NevisTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> RESOURCE_AUTHORITY_reqDATA()
  {
    Map<String, Map<NevisTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> result = new HashMap<>();

    // Account.Common
    result.put(prop.getResource().getCanUseUsername(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(prop.getResource().getCanUsePassword(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(prop.getResource().getGoogleCaptchaValidate(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(prop.getResource().getCreateAccount(), Map.of(ANY, Map.of(POST, Map.of())));

    // Account.Access
    result.put(prop.getResource().getGetAccountAccess(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(prop.getResource().getPasswordChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(prop.getResource().getPasswordResetReq(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(prop.getResource().getPasswordResetConfirm(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(prop.getResource().getPasswordReset(), Map.of(ANY, Map.of(POST, Map.of())));

    // Account.Email
    result.put(prop.getResource().getGetAccountEmail(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(prop.getResource().getEmailConfirmReq(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(prop.getResource().getEmailConfirm(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(prop.getResource().getEmailChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(prop.getResource().getUpdateAccountEmail(), Map.of(USER, Map.of(POST, Map.of())));

    // Account.Phone
    result.put(prop.getResource().getGetAccountPhone(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(prop.getResource().getPhoneChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(prop.getResource().getUpdateAccountPhone(), Map.of(USER, Map.of(POST, Map.of())));

    // Account.Personal
    result.put(prop.getResource().getGetAccountPersonal(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(prop.getResource().getNicknameChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(prop.getResource().getUpdateAccountPersonal(), Map.of(USER, Map.of(POST, Map.of())));

    return result;
  }


  //-------------------------------------------------------
  // Expected statuses:
  //    200 = OK
  //    400 = Bad Request
  //    401 = Unauthorized
  //    403 = Forbidden, access_denied

  static final Map<NevisTestAuthorityLevel, Map<NevisTestAuthorityLevel, Integer>> AUTHORITY_to_AUTHORITY_STATUS = Map.of(
          ANY, Map.of(
                  ANY, 200,
                  USER, 401,
                  ADMIN, 401),
          USER, Map.of(
                  ANY, 200,
                  USER, 200,
                  ADMIN, 403),
          ADMIN, Map.of(
                  ANY, 200,
                  USER, 200,
                  ADMIN, 200)
  );

  static final Map<NevisTestAuthorityLevel, Map<NevisTestAuthorityLevel, Integer>> AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN = Map.of(
          USER, Map.of(
                  ANY, 401,
                  USER, 401,
                  ADMIN, 401),
          ADMIN, Map.of(
                  ANY, 401,
                  USER, 401,
                  ADMIN, 401)
  );
}
