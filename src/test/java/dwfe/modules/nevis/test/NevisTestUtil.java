package dwfe.modules.nevis.test;

import dwfe.modules.nevis.config.NevisConfigProperties;
import dwfe.modules.nevis.util.NevisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dwfe.modules.nevis.test.NevisTestAuthType.REFRESH;
import static dwfe.modules.nevis.test.NevisTestResourceAccessingType.BAD_ACCESS_TOKEN;
import static dwfe.modules.nevis.test.NevisTestResourceAccessingType.USUAL;
import static dwfe.modules.nevis.test.NevisTestVariablesForAuthTest.AUTHORITY_to_AUTHORITY_STATUS;
import static dwfe.modules.nevis.test.NevisTestVariablesForAuthTest.AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;
import static dwfe.modules.nevis.util.NevisUtil.getJsonFromObj;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Component
public class NevisTestUtil
{
  private static final Logger log = LoggerFactory.getLogger(NevisTestUtil.class);
  private final NevisConfigProperties prop;
  private final NevisUtil nevisUtil;
  private final RestTemplateBuilder rtb;
  private final NevisTestVariablesForAuthTest VARS_FOR_AUTH_TESTS;

  private NevisTestUtil(NevisConfigProperties prop, NevisUtil nevisUtil, RestTemplateBuilder restTemplateBuilder, NevisTestVariablesForAuthTest varsForAuthTest)
  {
    this.prop = prop;
    this.nevisUtil = nevisUtil;
    this.rtb = restTemplateBuilder;
    this.VARS_FOR_AUTH_TESTS = varsForAuthTest;
  }


  //-------------------------------------------------------
  // Exchange Core
  //

  private Map<String, Object> performRequest(RestTemplate restTemplate, RequestEntity<?> req, int expectedStatus)
  {
    ResponseEntity<?> resp;
    Map<String, Object> body = null;
    var actualStatusCode = -1;

    log.info("= authorization: {}", req.getHeaders().get("Authorization"));
    log.info("-> " + req.getUrl());
    try
    {
      resp = restTemplate.exchange(req, new ParameterizedTypeReference<Map<String, Object>>()
      {
      });
      actualStatusCode = resp.getStatusCodeValue();

      body = (Map<String, Object>) resp.getBody();
      log.info("= expected: {}", expectedStatus);
      log.info("<- {} {}\n", actualStatusCode, body);
    }
    catch (Throwable e)
    {
      if (e instanceof HttpClientErrorException)
      {
        HttpClientErrorException httpError = (HttpClientErrorException) e;
        actualStatusCode = httpError.getRawStatusCode();
        log.error("<- {} {} {}\n",
                actualStatusCode,
                httpError.getStatusCode().getReasonPhrase(),
                httpError.getResponseBodyAsString());
      }
      else
        e.printStackTrace();
    }
    assertEquals(expectedStatus, actualStatusCode);
    return body;
  }

  private RequestEntity<?> generateGETrequest(String url, String access_token)
  {
    var reqBuilder = RequestEntity.get(URI.create(prop.getApiRoot() + url));
    if (access_token != null)
      reqBuilder.header("Authorization", "Bearer " + access_token);
    return reqBuilder.build();
  }

  private RequestEntity<?> generatePOSTrequest(String url, String access_token, Map<String, Object> map)
  {
    var reqBuilder = RequestEntity
            .post(URI.create(prop.getApiRoot() + url))
            .contentType(MediaType.APPLICATION_JSON_UTF8);
    if (access_token != null)
      reqBuilder.header("Authorization", "Bearer " + access_token);

    var body = getJsonFromObj(map);
    log.info("= body: {}", body);

    return reqBuilder.body(body);
  }

  private Map<String, Object> responseAfterPOSTrequest(String resource, String access_token, Map<String, Object> map, int expectedStatus)
  {
    var req = generatePOSTrequest(resource, access_token, map);
    return performRequest(rtb.build(), req, expectedStatus);
  }

  private Map<String, Object> responseAfterGETrequest(String resource, String access_token, int expectedStatus)
  {
    var req = generateGETrequest(resource, access_token);
    return performRequest(rtb.build(), req, expectedStatus);
  }

  public void check(RequestMethod method, String resource, String access_token, List<NevisTestChecker> checkers)
  {
    checkers.forEach(checker -> {
      Map<String, Object> body;
      if (GET == method)
        body = responseAfterGETrequest(resource, access_token, checker.expectedStatus);
      else
        body = responseAfterPOSTrequest(resource, access_token, checker.requestMap, checker.expectedStatus);

      assertEquals(checker.expectedResult, body.get("success"));

      if (checker.expectedError != null)
        assertEquals(checker.expectedError, ((List<String>) body.get("error-codes")).get(0));

      if (checker.expectedResponseMap != null)
      {
        log.info("= expected: " + getJsonFromObj(checker.expectedResponseMap));

        var details = (Map<String, Object>) body.get("data");
        if (details.containsKey("createdOn"))
          details.put("createdOn", "date");
        if (details.containsKey("updatedOn"))
          details.put("updatedOn", "date");

        assertEquals(checker.expectedResponseMap, details);
      }
    });
  }


  //-------------------------------------------------------
  // Exchange Processes
  //

  public void tokenProcess(NevisTestAuthType signInType, NevisTestAuth auth, int expectedStatus)
  {
    var url = prop.getResource().getSignIn();
    if (REFRESH == signInType)
    {
      url = String.format(url + "?grant_type=refresh_token&refresh_token=%s", auth.refresh_token);

      log.info("token refreshing");
      log.info("= refresh token: {}", auth.refresh_token);
    }
    else
    {
      url = String.format(url + "?grant_type=password&username=%1$s&password=%2$s%3$s",
              URLEncoder.encode(auth.username, StandardCharsets.UTF_8),
              URLEncoder.encode(auth.password, StandardCharsets.UTF_8),
              auth.usernameType == null ? "" : "&usernameType=" + auth.usernameType
      );

      log.info("signing in");
      log.info("= auth credentials:  {}:{}; usernameType={}", auth.username, auth.password, auth.usernameType);
    }
    RequestEntity<?> req = generatePOSTrequest(url, null, null);

    var client = auth.client;
    log.info("= client credentials:  {}:{}", client.clientname, client.clientpass);
    RestTemplate rt = rtb.basicAuthorization(client.clientname, client.clientpass).build();

    var body = performRequest(rt, req, expectedStatus);

    if (expectedStatus == 200)
    {
      var access_token = (String) body.get("access_token");
      var refresh_token = (String) body.get("refresh_token");

      assertThat(access_token.length(), greaterThan(0));
      assertThat(refresh_token.length(), greaterThan(0));
      assertThat((int) body.get("expires_in"),
              is(both(greaterThan(client.minTokenExpirationTime)).and(lessThanOrEqualTo(client.maxTokenExpirationTime))));

      auth.access_token = access_token;
      auth.refresh_token = refresh_token;
    }
  }

  public void fullAuthProcess(NevisTestAuth auth)
  {
    //1,2,3
    resourceAccessing_with_changeToken_Process(auth);

    //4. Sign Out
    signOutProcess(auth);

    //5. Resource accessing
    resourceAccessingProcess(auth.access_token, auth.authorityLevel, BAD_ACCESS_TOKEN);

    //6. Change Token
    tokenProcess(REFRESH, auth, 400);
  }

  private void resourceAccessing_with_changeToken_Process(NevisTestAuth auth)
  {
    //1. Resource accessing
    resourceAccessingProcess(auth.access_token, auth.authorityLevel, USUAL);

    //2. Change Token
    var old_access_token = auth.access_token;
    var old_refresh_token = auth.refresh_token;
    var expectedStatus = auth.client.clientname.equals(prop.getOauth2ClientTrusted().getId()) ? 200 : 401;
    tokenProcess(REFRESH, auth, expectedStatus);
    if (expectedStatus == 200)
    {
      assertNotEquals(old_access_token, auth.access_token);
      assertEquals(old_refresh_token, auth.refresh_token);

      //3. Resource accessing: old/new token
      resourceAccessingProcess(old_access_token, auth.authorityLevel, BAD_ACCESS_TOKEN);
      resourceAccessingProcess(auth.access_token, auth.authorityLevel, USUAL);
    }
    else
    {
      resourceAccessingProcess(auth.access_token, auth.authorityLevel, BAD_ACCESS_TOKEN);
    }
  }

  public void resourceAccessingProcess(String access_token, NevisTestAuthorityLevel authorityLevel, NevisTestResourceAccessingType resourceAccessingType)
  {
    Map<NevisTestAuthorityLevel, Map<NevisTestAuthorityLevel, Integer>> statusMap;

    if (BAD_ACCESS_TOKEN == resourceAccessingType)
      statusMap = AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;
    else
      statusMap = AUTHORITY_to_AUTHORITY_STATUS;

    VARS_FOR_AUTH_TESTS.RESOURCE_AUTHORITY_reqDATA().forEach((resource, next) -> {

      try
      {
        TimeUnit.MILLISECONDS.sleep(70);
      }
      catch (InterruptedException ignored)
      {
      }

      var next1 = next.entrySet().iterator().next();
      var next2 = next1.getValue().entrySet().iterator().next();

      var level = next1.getKey();
      var method = next2.getKey();
      var map = next2.getValue();
      var expectedStatus = statusMap.get(authorityLevel).get(level);

      if (GET == method)
        responseAfterGETrequest(resource, access_token, expectedStatus);
      else
        responseAfterPOSTrequest(resource, access_token, map, expectedStatus);
    });
  }

  private void signOutProcess(NevisTestAuth auth)
  {
    log.info("sign out");
    var expectedStatus = auth.client.clientname.equals(prop.getOauth2ClientTrusted().getId()) ? 200 : 401;
    var body = responseAfterGETrequest(prop.getResource().getSignOut(), auth.access_token, expectedStatus);
    if (expectedStatus == 200)
    {
      assertNotEquals(null, body);
      assertEquals(true, body.get("success"));
    }
  }
}
