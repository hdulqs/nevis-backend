package dwfe.controller;

import dwfe.config.DwfeConfigProperties;
import dwfe.util.DwfeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static dwfe.util.DwfeUtil.getResponse;
import static dwfe.util.DwfeUtil.isDefaultPreCheckOk;

@RestController
@RequestMapping(path = "#{dwfeConfigProperties.api}", produces = "application/json; charset=utf-8")
public class DwfeControllerV1
{
  private final DwfeUtil utilDwfe;
  private final DwfeConfigProperties propDwfe;

  @Autowired
  public DwfeControllerV1(DwfeUtil utilDwfe, DwfeConfigProperties propDwfe)
  {
    this.utilDwfe = utilDwfe;
    this.propDwfe = propDwfe;
  }

  @PostMapping("#{dwfeConfigProperties.resource.googleCaptchaValidate}")
  public String googleCaptchaValidate(@RequestBody ReqGoogleCaptchaResponse req)
  {
    var errorCodes = new ArrayList<String>();
    var errName = "google-captcha";

    if (isDefaultPreCheckOk(req.googleResponse, errName, errorCodes))
    {
      if (propDwfe.getCaptcha() != null && propDwfe.getCaptcha().getGoogleSecretKey() != null)
      {
        // https://developers.google.com/recaptcha/docs/verify#api-request
        var url = String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s",
                propDwfe.getCaptcha().getGoogleSecretKey(), req.googleResponse);
        var body = utilDwfe.exchangeWrap(url, HttpMethod.POST, 3, errName, errorCodes);
        if (errorCodes.size() == 0)
        {
          var success = (Boolean) body.get("success");
          if (!success)
            errorCodes.add(errName + "-detected-robot");
        }
      }
      else errorCodes.add(errName + "-not-initialized");
    }
    return getResponse(errorCodes);
  }
}

class ReqGoogleCaptchaResponse
{
  String googleResponse;

  public String getGoogleResponse()
  {
    return googleResponse;
  }

  public void setGoogleResponse(String googleResponse)
  {
    this.googleResponse = googleResponse;
  }
}

