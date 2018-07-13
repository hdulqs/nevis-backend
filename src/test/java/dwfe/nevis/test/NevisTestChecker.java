package dwfe.nevis.test;

import java.util.Map;

public class NevisTestChecker
{
  Boolean expectedResult;
  int expectedStatus;
  Map<String, Object> requestMap;
  String expectedError;
  Map<String, Object> expectedResponseMap;

  static NevisTestChecker of(Boolean expectedResult, int expectedStatus,
                             Map<String, Object> requestMap,
                             String expectedError)
  {
    var checker = new NevisTestChecker();
    checker.expectedResult = expectedResult;
    checker.expectedStatus = expectedStatus;
    checker.requestMap = requestMap;
    checker.expectedError = expectedError;
    return checker;
  }

  static NevisTestChecker of(Boolean expectedResult, int expectedStatus,
                             Map<String, Object> requestMap)
  {
    var checker = new NevisTestChecker();
    checker.expectedResult = expectedResult;
    checker.expectedStatus = expectedStatus;
    checker.requestMap = requestMap;
    return checker;
  }

  static NevisTestChecker of(Boolean expectedResult, int expectedStatus,
                             Map<String, Object> req,
                             Map<String, Object> expectedResponseMap)
  {
    var checker = new NevisTestChecker();
    checker.expectedResult = expectedResult;
    checker.expectedStatus = expectedStatus;
    checker.requestMap = req;
    checker.expectedResponseMap = expectedResponseMap;
    return checker;
  }
}
