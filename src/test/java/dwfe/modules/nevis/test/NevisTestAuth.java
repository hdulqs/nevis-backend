package dwfe.modules.nevis.test;

import dwfe.modules.nevis.db.account.access.NevisAccountUsernameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dwfe.modules.nevis.db.account.access.NevisAccountUsernameType.ID;
import static dwfe.modules.nevis.db.account.access.NevisAccountUsernameType.PHONE;
import static dwfe.modules.nevis.test.NevisTestAuthType.SIGN_IN;
import static dwfe.modules.nevis.test.NevisTestAuthorityLevel.ADMIN;
import static dwfe.modules.nevis.test.NevisTestAuthorityLevel.USER;
import static dwfe.modules.nevis.test.NevisTestVariablesForIntegrationTest.*;

@Component
public class NevisTestAuth
{
  @Autowired
  private NevisTestUtil util;
  @Autowired
  private NevisTestClient nevisTestClient;

  public String username;
  public NevisAccountUsernameType usernameType;
  public String password;

  NevisTestClient client;

  public NevisTestAuthorityLevel authorityLevel;

  public String access_token;
  String refresh_token;


  public NevisTestAuth of(NevisTestAuthorityLevel authorityLevel,
                          String username, NevisAccountUsernameType usernameType,
                          String password,
                          NevisTestClient client)
  {
    var auth = new NevisTestAuth();
    auth.authorityLevel = authorityLevel;
    auth.username = username;
    auth.usernameType = usernameType;
    auth.password = password;
    auth.client = client;

    setToken(auth);
    return auth;
  }

  public NevisTestAuth of(String username, NevisAccountUsernameType usernameType,
                          String password,
                          NevisTestClient client)
  {
    var auth = new NevisTestAuth();
    auth.authorityLevel = USER;
    auth.username = username;
    auth.usernameType = usernameType;
    auth.password = password;
    auth.client = client;

    return auth;
  }


  private void setToken(NevisTestAuth auth)
  {
    util.tokenProcess(SIGN_IN, auth, 200);
  }

  public NevisTestAuth getAnonymous()
  {
    var auth = new NevisTestAuth();
    auth.authorityLevel = NevisTestAuthorityLevel.ANY;
    return auth;
  }

  public NevisTestAuth getADMIN()
  {
    return of(ADMIN, Account1_ID, ID, Account1_Pass, nevisTestClient.getClientUntrusted());
  }

  String getADMIN_accessToken()
  {
    return getADMIN().access_token;
  }

  public NevisTestAuth getUSER()
  {
    return of(USER, Account2_PHONE, PHONE, Account2_Pass, nevisTestClient.getClientTrusted());
  }

  String getUSER_accessToken()
  {
    return getUSER().access_token;
  }

  public String getAnonym_accessToken()
  {
    return getAnonymous().access_token;
  }
}

