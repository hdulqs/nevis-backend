package dwfe.modules.nevis.test;

import dwfe.config.DwfeConfigProperties;
import dwfe.modules.nevis.config.NevisConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import static dwfe.modules.nevis.test.NevisTestAuthorityLevel.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Configuration
public class NevisTestVariablesForAuthTest
{
  @Autowired
  private DwfeConfigProperties propDwfe;
  @Autowired
  private NevisConfigProperties propNevis;

  //-------------------------------------------------------
  // RESOURCES
  //

  public Map<String, Map<NevisTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> RESOURCE_AUTHORITY_reqDATA()
  {
    Map<String, Map<NevisTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> result = new HashMap<>();

    // DWFE
    result.put(propDwfe.getResource().getGoogleCaptchaValidate(), Map.of(ANY, Map.of(POST, Map.of())));

    // Account.Common
    result.put(propNevis.getResource().getCanUseUsername(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getCanUsePassword(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getCreateAccount(), Map.of(ANY, Map.of(POST, Map.of())));

    // Account.Access
    result.put(propNevis.getResource().getGetAccountAccess(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(propNevis.getResource().getPasswordChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getPasswordResetReq(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getPasswordReset(), Map.of(ANY, Map.of(POST, Map.of())));

    // Account.Email
    result.put(propNevis.getResource().getGetAccountEmail(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(propNevis.getResource().getEmailConfirmReq(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(propNevis.getResource().getEmailConfirm(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getEmailChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getUpdateAccountEmail(), Map.of(USER, Map.of(POST, Map.of())));

    // Account.Phone
    result.put(propNevis.getResource().getGetAccountPhone(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(propNevis.getResource().getPhoneChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getUpdateAccountPhone(), Map.of(USER, Map.of(POST, Map.of())));

    // Account.Personal
    result.put(propNevis.getResource().getGetAccountPersonal(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(propNevis.getResource().getNicknameChange(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(propNevis.getResource().getUpdateAccountPersonal(), Map.of(USER, Map.of(POST, Map.of())));

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
