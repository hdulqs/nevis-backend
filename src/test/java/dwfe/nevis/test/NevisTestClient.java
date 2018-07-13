package dwfe.nevis.test;

import dwfe.nevis.config.NevisConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NevisTestClient
{
  @Autowired
  private NevisConfigProperties prop;

  String clientname;
  String clientpass;
  int maxTokenExpirationTime;
  int minTokenExpirationTime;

  NevisTestClient of(String clientname, String clientpass, int maxTokenExpirationTime, int minTokenExpirationTime)
  {
    var client = new NevisTestClient();
    client.clientname = clientname;
    client.clientpass = clientpass;
    client.maxTokenExpirationTime = maxTokenExpirationTime;
    client.minTokenExpirationTime = minTokenExpirationTime;
    return client;
  }

  public NevisTestClient getClientTrusted()
  {
    return of(
            prop.getOauth2ClientTrusted().getId(),
            prop.getOauth2ClientTrusted().getPassword(),
            1_728_000,
            180
    );
  }

  public NevisTestClient getClientUntrusted()
  {
    return of(
            prop.getOauth2ClientUntrusted().getId(),
            prop.getOauth2ClientUntrusted().getPassword(),
            180,
            0
    );
  }
}
