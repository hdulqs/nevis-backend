package dwfe.nevis.test;

import org.springframework.boot.json.JsonParserFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dwfe.nevis.util.NevisUtil.isStringBcrypted;

public class NevisTestVariablesForIntegrationTest
{
  public static final String Account1_ID = "1000";
  public static final String Account1_EMAIL = "test1@dwfe.ru";
  public static final String Account1_Pass = "test11";

  public static final String Account2_ID = "1001";
  public static final String Account2_EMAIL = "test2@dwfe.ru";
  public static final String Account2_NICKNAME = "test2";
  public static final String Account2_PHONE = "+79094141719";
  public static final String Account2_Pass = "test22";

  public static final String Account3_EMAIL = "test3@dwfe.ru";
  public static String Account3_Pass; //will be set during the testing process

  public static Long Account4_ID; //will be set during the testing process
  public static final String Account4_EMAIL = "test4@dwfe.ru";
  public static final String Account4_NICKNAME = "test4";
  public static final String Account4_NewNickName = "test44";
  public static final String Account4_Pass = "test44";
  public static final String Account4_NewPass = "1234567890";

  public static final String Account5_EMAIL = "test5@dwfe.ru";
  public static final String Account5_PHONE = "+1 223-22-1";
  public static final String Account5_NewPhone = "+1 551-44-3";
  public static final String Account5_Pass = "test55";

  public static Long Account6_ID; //will be set during the testing process
  public static final String Account6_EMAIL = "test6@dwfe.ru";
  public static final String Account6_NewEmail = "test77@dwfe.ru";
  public static final String Account6_Pass = "test66";
  public static final String Account6_NewPass = "1234567890";

  public static Long Account7_ID; //will be set during the testing process
  public static final String Account7_EMAIL = "test7@dwfe.ru";
  public static final String Account7_NICKNAME = "test7";
  public static final String Account7_PHONE = "+2 554-44-32";
  public static final String Account7_Pass_Decoded = "hello123world";
  public static final String Account7_Pass_Encoded = "$2a$10$AvHrvvqQNOyUZxg7XMfDleDLjR3AV5C1KEwsa29EC4Eo7CYIe0eoy"; //hello123world
  public static final String Account7_NewPass_Decoded = "56789900aloha";
  private static final String Account7_NewPass_Encoded = "$2a$10$EGQlh6wWYUFrVbnZJzMvwOnGSxlS65Oap.6l92nA3PLskkipat7Di"; //56789900aloha

  public static final String Account8_NICKNAME = "test8";
  public static final String Account8_EMAIL = "test8@dwfe.ru";
  public static final String Account8_Pass = "test88";

  public static final String Account9_NICKNAME = "test9";
  public static final String Account9_Pass = "test99";


  //-------------------------------------------------------
  // Account.Common
  //

  public static final List<NevisTestChecker> checkers_for_canUseUsername = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-username"),
          NevisTestChecker.of(false, 200, Map.of("username", ""), "empty-username"),

          NevisTestChecker.of(false, 200, Map.of("username", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678907", "usernameType", "NICKNAME"), "exceeded-max100-nickName-length"),
          NevisTestChecker.of(false, 200, Map.of("username", "test1", "usernameType", "NICKNAME"), "nickName-present-in-database"),

          NevisTestChecker.of(false, 200, Map.of("username", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678907", "usernameType", "PHONE"), "exceeded-max100-phone-length"),
          NevisTestChecker.of(false, 200, Map.of("username", "+79094141719", "usernameType", "PHONE"), "phone-present-in-database"),

          NevisTestChecker.of(false, 200, Map.of("username", "id", "usernameType", "ID"), "id-must-be-a-positive-number"),
          NevisTestChecker.of(false, 200, Map.of("username", "-1", "usernameType", "ID"), "id-must-be-a-positive-number"),
          NevisTestChecker.of(false, 200, Map.of("username", "9223372036854775808", "usernameType", "ID"), "id-must-be-of-type-long"),
          NevisTestChecker.of(false, 200, Map.of("username", "1000", "usernameType", "ID"), "id-present-in-database"),

          NevisTestChecker.of(false, 200, Map.of("username", "89012345678901234567890kkklkklklklkklklklklklklklk89012345678901234567890kkklkklklklkklklk1@gmail.com", "usernameType", "EMAIL"), "exceeded-max100-email-length"),
          NevisTestChecker.of(false, 200, Map.of("username", "@ss@ds.ru", "usernameType", "EMAIL"), "invalid-email"),
          NevisTestChecker.of(false, 200, Map.of("username", "@ss@ds.ru"), "invalid-email"),
          NevisTestChecker.of(false, 200, Map.of("username", Account1_EMAIL, "usernameType", "EMAIL"), "email-present-in-database"),

          NevisTestChecker.of(true, 200, Map.of("username", Account3_EMAIL, "usernameType", "EMAIL"))
  );

  public static final List<NevisTestChecker> checkers_for_canUsePassword = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-password"),
          NevisTestChecker.of(false, 200, Map.of("password", ""), "empty-password"),
          NevisTestChecker.of(false, 200, Map.of("password", "12345"), "out-of-range-min6-max55-password-length"),
          NevisTestChecker.of(false, 200, Map.of("password", "12345678901234567890123456789012345678901234567890123456"), "out-of-range-min6-max55-password-length"),
          NevisTestChecker.of(true, 200, Map.of("password", "123456")),
          NevisTestChecker.of(true, 200, Map.of("password", Account7_Pass_Encoded))
  );

  public static List<NevisTestChecker> checkers_for_createAccount()
  {
    var list = new ArrayList<NevisTestChecker>(List.of(
            NevisTestChecker.of(false, 200, Map.of(), "username-must-be-passed"),
            NevisTestChecker.of(false, 200, Map.of("nickName", "nibi"), "the-generated-password-should-be-sent-somewhere"),

            NevisTestChecker.of(false, 200, Map.of("email", "123@sd.tt", "password", ""), "empty-password"),
            NevisTestChecker.of(false, 200, Map.of("email", "123@sd.tt", "password", "12345"), "out-of-range-min6-max55-password-length"),
            NevisTestChecker.of(false, 200, Map.of("phone", "567", "password", "12345678901234567890123456789012345678901234567890123456"), "out-of-range-min6-max55-password-length"),

            NevisTestChecker.of(false, 200, Map.of("email", "m123@ys.er", "gender", ""), "empty-gender"),
            NevisTestChecker.of(false, 200, Map.of("email", "user@er.ww", "gender", "d"), "invalid-gender"),

            NevisTestChecker.of(false, 200, Map.of("email", "user@er.ww", "dateOfBirth", ""), "empty-dateOfBirth"),
            NevisTestChecker.of(false, 200, Map.of("email", "user@er.ww", "dateOfBirth", "1980-1-01"), "dateOfBirth-cannot-be-parsed"),

            NevisTestChecker.of(false, 200, Map.of("email", "m123@ys.er", "country", ""), "empty-country"),
            NevisTestChecker.of(false, 200, Map.of("email", "m123@ys.er", "country", "d"), "invalid-country"),

            NevisTestChecker.of(false, 200, Map.of("email", ""), "empty-email"),
            NevisTestChecker.of(false, 200, Map.of("email", "89012345678901234567890kkklkklklklkklklklklklklklk89012345678901234567890kkklkklklklkklklk1@gmail.com"), "exceeded-max100-email-length"),
            NevisTestChecker.of(false, 200, Map.of("email", "89012345678901234567890kkklkklklklkklklklklklklklk89012345678901234567890kkklkklklklkklklklklklklklk"), "invalid-email"),
            NevisTestChecker.of(false, 200, Map.of("email", Account1_EMAIL), "email-present-in-database"),

            NevisTestChecker.of(false, 200, Map.of("nickName", "", "password", "12345678"), "empty-nickName"),
            NevisTestChecker.of(false, 200, Map.of("nickName", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678909", "password", "12345678"), "exceeded-max100-nickName-length"),
            NevisTestChecker.of(false, 200, Map.of("nickName", "test2", "password", "12345678"), "nickName-present-in-database"),

            NevisTestChecker.of(false, 200, Map.of("email", "m123@ys.er", "phone", ""), "empty-phone"),
            NevisTestChecker.of(false, 200, Map.of("email", "m123@ys.er", "phone", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678909"), "exceeded-max100-phone-length"),
            NevisTestChecker.of(false, 200, Map.of("email", "m123@ys.er", "phone", "+79094141719"), "phone-present-in-database")
    ));
    list.addAll(List.of(
            NevisTestChecker.of(true, 200, Map.of(
                    "email", Account3_EMAIL,
                    "firstName", "ozon",
                    "middleName", "Alice",
                    "lastName", "sunshine")),

            NevisTestChecker.of(true, 200, Map.of(
                    "nickName", Account4_NICKNAME,
                    "password", Account4_Pass,
                    "gender", "m",
                    "dateOfBirth", "1980-11-27",
                    "country", "De")),

            NevisTestChecker.of(true, 200, Map.of(
                    "phone", Account5_PHONE,
                    "password", Account5_Pass,
                    "city", "Dallas",
                    "company", "Home Ltd.",
                    "positionHeld", "programmer")),

            NevisTestChecker.of(true, 200, Map.of(
                    "email", Account6_EMAIL,
                    "password", Account6_Pass)),

            NevisTestChecker.of(true, 200, Map.of(
                    "email", Account7_EMAIL,
                    "password", Account7_Pass_Encoded,
                    "phone", "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                    "nickName", "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                    "firstName", "12345678901234567890777777",
                    "middleName", "12345678901234567890777777",
                    "lastName", "12345678901234567890777777",
                    "city", "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789077777",
                    "company", "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789077777",
                    "positionHeld", "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789077777"))
    ));
    return list;
  }

  public static final List<NevisTestChecker> checkers_for_id1 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "id-not-exist")
  );

  public static final List<NevisTestChecker> checkers_for_id2 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), Map.of(
                  "access", JsonParserFactory.getJsonParser().parseMap(
                          "{\"id\":1000}"
                  ),
                  "email", JsonParserFactory.getJsonParser().parseMap(
                          "{\"value\":\"test1@dwfe.ru\"}"
                  ),
                  "phone", JsonParserFactory.getJsonParser().parseMap(
                          "{\"value\":\"+79990011273\"}"
                  ),
                  "personal", JsonParserFactory.getJsonParser().parseMap(
                          "{\"nickName\":\"test1\"," +
                                  "\"firstName\":null," +
                                  "\"middleName\":null," +
                                  "\"lastName\":null," +
                                  "\"gender\":null," +
                                  "\"dateOfBirth\":null," +
                                  "\"country\":null," +
                                  "\"city\":null," +
                                  "\"company\":null," +
                                  "\"positionHeld\":null}"
                  )
          ))
  );

  public static final List<NevisTestChecker> checkers_for_id3 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), Map.of(
                  "access", JsonParserFactory.getJsonParser().parseMap(
                          "{\"id\":1001}"
                  ),
                  "email", Map.of(),
                  "phone", Map.of(),
                  "personal", Map.of()
          ))
  );

  public static final List<NevisTestChecker> checkers_for_deleteAccount = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", ""), "empty-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1"), "wrong-curpass"),
          NevisTestChecker.of(true, 200, Map.of("curpass", Account1_Pass))
  );


  //-------------------------------------------------------
  // Account.Access
  //

  public static final List<NevisTestChecker> checkers_for_getAccountAccess = List.of(
          NevisTestChecker.of(true, 200, Map.of(), Map.of(
                  "id", Integer.parseInt(Account1_ID),
                  "authorities", List.of("USER", "ADMIN"),
                  "accountNonExpired", true,
                  "credentialsNonExpired", true,
                  "accountNonLocked", true,
                  "enabled", true,
                  "createdOn", "date",
                  "updatedOn", "date"
          ))
  );

  public static final List<NevisTestChecker> checkers_for_passwordChange = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", ""), "empty-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account6_Pass), "missing-newpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account6_Pass, "newpass", ""), "empty-newpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account6_Pass, "newpass", "12345"), "out-of-range-min6-max55-newpass-length"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account6_Pass, "newpass", "12345678901234567890123456789012345678901234567890123456"), "out-of-range-min6-max55-newpass-length"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account6_NewPass, "newpass", Account6_NewPass), "wrong-curpass"),
          NevisTestChecker.of(true, 200, Map.of("curpass", Account6_Pass, "newpass", Account6_NewPass))
  );

  public static final List<NevisTestChecker> checkers_for_passwordChange_2 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", ""), "empty-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account7_Pass_Decoded), "missing-newpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account7_Pass_Decoded, "newpass", ""), "empty-newpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", Account7_NewPass_Encoded, "newpass", Account7_NewPass_Encoded), "wrong-curpass"),
          NevisTestChecker.of(true, 200, Map.of("curpass", Account7_Pass_Decoded, "newpass", Account7_NewPass_Encoded))
  );

  public static List<NevisTestChecker> checkers_for_passwordResetReq(String email)
  {
    return List.of(
            NevisTestChecker.of(false, 200, Map.of(), "missing-email"),
            NevisTestChecker.of(false, 200, Map.of("email", ""), "empty-email"),
            NevisTestChecker.of(false, 200, Map.of("email", "89012345678901234567890kkklkklklklkklklklklklklklk89012345678901234567890kkklkklklklkklklk1@gmail.com"), "exceeded-max100-email-length"),
            NevisTestChecker.of(false, 200, Map.of("email", "@uu@gg.ru"), "invalid-email"),
            NevisTestChecker.of(false, 200, Map.of("email", "uu@gg.ru"), "email-not-exist"),
            NevisTestChecker.of(true, 200, Map.of("email", email))
    );
  }

  public static List<NevisTestChecker> checkers_for_passwordResetReq_duplicateDelay(String email)
  {
    return List.of(
            NevisTestChecker.of(false, 200, Map.of("email", email), "delay-between-duplicate-requests")
    );
  }

  public static List<NevisTestChecker> checkers_for_passwordReset(String username, String newpass, String existedKey)
  {
    var list = new ArrayList<NevisTestChecker>(List.of(
            NevisTestChecker.of(false, 200, Map.of(), "missing-newpass"),
            NevisTestChecker.of(false, 200, Map.of("newpass", ""), "empty-newpass")
    ));

    if (!isStringBcrypted(newpass))
    {
      list.addAll(List.of(
              NevisTestChecker.of(false, 200, Map.of("newpass", "54321"), "out-of-range-min6-max55-newpass-length"),
              NevisTestChecker.of(false, 200, Map.of("newpass", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), "out-of-range-min6-max55-newpass-length")
      ));
    }
    list.addAll(List.of(
            NevisTestChecker.of(false, 200, Map.of("newpass", newpass), "missing-confirm-key"),
            NevisTestChecker.of(false, 200, Map.of("newpass", newpass, "key", ""), "empty-confirm-key"),
            NevisTestChecker.of(false, 200, Map.of("newpass", newpass, "key", existedKey + "w"), "confirm-key-not-exist"),
            NevisTestChecker.of(true, 200, Map.of("newpass", newpass, "key", existedKey), Map.of(
                    "username", username
            ))
    ));
    return list;
  }


  //-------------------------------------------------------
  // Account.Email
  //

  public static final List<NevisTestChecker> checkers_for_getAccountEmail1 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "account-is-not-linked-to-email")
  );

  public static final List<NevisTestChecker> checkers_for_getAccountEmail2 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), Map.of(
                  "value", Account1_EMAIL,
                  "nonPublic", false,
                  "confirmed", true,
                  "updatedOn", "date"
          ))
  );

  public static final List<NevisTestChecker> checkers_for_emailConfirmReq_noEmailAssociatedWithAccount = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "no-email-associated-with-account")
  );

  public static final List<NevisTestChecker> checkers_for_emailConfirmReq_isAlreadyConfirmed = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "email-is-already-confirmed")
  );

  public static final List<NevisTestChecker> checkers_for_emailConfirmReq = List.of(
          NevisTestChecker.of(true, 200, Map.of())
  );

  public static final List<NevisTestChecker> checkers_for_emailConfirmReq_duplicateDelay = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "delay-between-duplicate-requests")
  );

  public static List<NevisTestChecker> checkers_for_confirmEmail(String existedKey)
  {
    return List.of(
            NevisTestChecker.of(false, 200, Map.of(), "missing-confirm-key"),
            NevisTestChecker.of(false, 200, Map.of("key", ""), "empty-confirm-key"),
            NevisTestChecker.of(false, 200, Map.of("key", "123"), "confirm-key-not-exist"),
            NevisTestChecker.of(true, 200, Map.of("key", existedKey))
    );
  }

  public static final List<NevisTestChecker> checkers_for_emailChange1 = List.of(
          NevisTestChecker.of(true, 200, Map.of("curpass", Account5_Pass, "newemail", Account5_EMAIL))
  );

  public static final List<NevisTestChecker> checkers_for_emailChange2 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", ""), "empty-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1"), "missing-newemail"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newemail", ""), "empty-newemail"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newemail", "89012345678901234567890kkklkklklklkklklklklklklklk89012345678901234567890kkklkklklklkklklk1@gmail.com"), "exceeded-max100-email-length"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newemail", "@dd.ss"), "invalid-email"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newemail", Account6_EMAIL), "email-present-in-database"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newemail", Account6_NewEmail), "wrong-curpass"),
          NevisTestChecker.of(true, 200, Map.of("curpass", Account6_Pass, "newemail", Account6_NewEmail))
  );

  public static final List<NevisTestChecker> checkers_for_updateAccountEmail1 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "no-email-associated-with-account")
  );

  public static final List<NevisTestChecker> checkers_for_updateAccountEmail2 = List.of(
          NevisTestChecker.of(true, 200, Map.of("nonPublic", false))
  );


  //-------------------------------------------------------
  // Account.Phone
  //

  public static final List<NevisTestChecker> checkers_for_getAccountPhone1 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "account-is-not-linked-to-phone")
  );

  public static final List<NevisTestChecker> checkers_for_getAccountPhone2 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), Map.of(
                  "value", Account5_PHONE,
                  "nonPublic", true,
                  "confirmed", false,
                  "updatedOn", "date"
          ))
  );

  public static final List<NevisTestChecker> checkers_for_phoneChange1 = List.of(
          NevisTestChecker.of(true, 200, Map.of("curpass", Account7_Pass_Decoded, "newphone", Account7_PHONE))
  );

  public static final List<NevisTestChecker> checkers_for_phoneChange2 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", ""), "empty-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1"), "missing-newphone"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newphone", ""), "empty-newphone"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newphone", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678907"), "exceeded-max100-phone-length"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newphone", Account5_PHONE), "phone-present-in-database"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newphone", Account5_NewPhone), "wrong-curpass"),
          NevisTestChecker.of(true, 200, Map.of("curpass", Account5_Pass, "newphone", Account5_NewPhone))
  );

  public static final List<NevisTestChecker> checkers_for_updateAccountPhone1 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "no-phone-associated-with-account")
  );

  public static final List<NevisTestChecker> checkers_for_updateAccountPhone2 = List.of(
          NevisTestChecker.of(true, 200, Map.of("nonPublic", false))
  );


  //-------------------------------------------------------
  // Account.Personal
  //

  public static final List<NevisTestChecker> checkers_for_getAccountPersonal1 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), JsonParserFactory.getJsonParser().parseMap(
                  "{\"nickName\":null," +
                          "\"nickNameNonPublic\":true," +
                          "\"firstName\":null," +
                          "\"firstNameNonPublic\":true," +
                          "\"middleName\":null," +
                          "\"middleNameNonPublic\":true," +
                          "\"lastName\":null," +
                          "\"lastNameNonPublic\":true," +
                          "\"gender\":null," +
                          "\"genderNonPublic\":true," +
                          "\"dateOfBirth\":null," +
                          "\"dateOfBirthNonPublic\":true," +
                          "\"country\":null," +
                          "\"countryNonPublic\":true," +
                          "\"city\":null," +
                          "\"cityNonPublic\":true," +
                          "\"company\":null," +
                          "\"companyNonPublic\":true," +
                          "\"positionHeld\":null," +
                          "\"positionHeldNonPublic\":true," +
                          "\"updatedOn\":\"date\"" +
                          "}"))
  );

  public static final List<NevisTestChecker> checkers_for_getAccountPersonal2 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), JsonParserFactory.getJsonParser().parseMap(
                  "{\"nickName\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                          "\"nickNameNonPublic\":true," +
                          "\"firstName\":\"12345678901234567890\"," +
                          "\"firstNameNonPublic\":true," +
                          "\"middleName\":\"12345678901234567890\"," +
                          "\"middleNameNonPublic\":true," +
                          "\"lastName\":\"12345678901234567890\"," +
                          "\"lastNameNonPublic\":true," +
                          "\"gender\":null," +
                          "\"genderNonPublic\":true," +
                          "\"dateOfBirth\":null," +
                          "\"dateOfBirthNonPublic\":true," +
                          "\"country\":null," +
                          "\"countryNonPublic\":true," +
                          "\"city\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                          "\"cityNonPublic\":true," +
                          "\"company\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                          "\"companyNonPublic\":true," +
                          "\"positionHeld\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                          "\"positionHeldNonPublic\":true," +
                          "\"updatedOn\":\"date\"" +
                          "}"))
  );

  public static final List<NevisTestChecker> checkers_for_getAccountPersonal3 = List.of(
          NevisTestChecker.of(true, 200, Map.of(), JsonParserFactory.getJsonParser().parseMap(
                  "{\"nickName\":\"" + Account4_NICKNAME + "\"," +
                          "\"nickNameNonPublic\":true," +
                          "\"firstName\":null," +
                          "\"firstNameNonPublic\":true," +
                          "\"middleName\":null," +
                          "\"middleNameNonPublic\":true," +
                          "\"lastName\":null," +
                          "\"lastNameNonPublic\":true," +
                          "\"gender\":\"M\"," +
                          "\"genderNonPublic\":true," +
                          "\"dateOfBirth\":\"1980-11-27\"," +
                          "\"dateOfBirthNonPublic\":true," +
                          "\"country\":\"DE\"," +
                          "\"countryNonPublic\":true," +
                          "\"city\":null," +
                          "\"cityNonPublic\":true," +
                          "\"company\":null," +
                          "\"companyNonPublic\":true," +
                          "\"positionHeld\":null," +
                          "\"positionHeldNonPublic\":true," +
                          "\"updatedOn\":\"date\"" +
                          "}"))
  );

  public static final List<NevisTestChecker> checkers_for_nicknameChange1 = List.of(
          NevisTestChecker.of(true, 200, Map.of("curpass", Account4_Pass, "newNickName", Account4_NewNickName))
  );

  public static final List<NevisTestChecker> checkers_for_nicknameChange2 = List.of(
          NevisTestChecker.of(false, 200, Map.of(), "missing-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", ""), "empty-curpass"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1"), "missing-newNickName"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newNickName", ""), "empty-newNickName"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newNickName", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678907"), "exceeded-max100-nickName-length"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newNickName", Account2_NICKNAME), "nickName-present-in-database"),
          NevisTestChecker.of(false, 200, Map.of("curpass", "1", "newNickName", Account7_NICKNAME), "wrong-curpass"),
          NevisTestChecker.of(true, 200, Map.of("curpass", Account7_Pass_Decoded, "newNickName", Account7_NICKNAME))
  );

  public static final List<NevisTestChecker> checkers_for_updateAccountPersonal = List.of(
          NevisTestChecker.of(false, 200, Map.of("gender", ""), "empty-gender"),
          NevisTestChecker.of(false, 200, Map.of("gender", "k"), "invalid-gender"),
          NevisTestChecker.of(false, 200, Map.of("dateOfBirth", ""), "empty-dateOfBirth"),
          NevisTestChecker.of(false, 200, Map.of("dateOfBirth", "2018-7-11"), "dateOfBirth-cannot-be-parsed"),
          NevisTestChecker.of(false, 200, Map.of("country", ""), "empty-country"),
          NevisTestChecker.of(false, 200, Map.of("country", "f"), "invalid-country"),

          NevisTestChecker.of(true, 200, Map.of(), JsonParserFactory.getJsonParser().parseMap(
                  "{\"nickName\":null," +
                          "\"nickNameNonPublic\":true," +
                          "\"firstName\":null," +
                          "\"firstNameNonPublic\":true," +
                          "\"middleName\":null," +
                          "\"middleNameNonPublic\":true," +
                          "\"lastName\":null," +
                          "\"lastNameNonPublic\":true," +
                          "\"gender\":null," +
                          "\"genderNonPublic\":true," +
                          "\"dateOfBirth\":null," +
                          "\"dateOfBirthNonPublic\":true," +
                          "\"country\":null," +
                          "\"countryNonPublic\":true," +
                          "\"city\":null," +
                          "\"cityNonPublic\":true," +
                          "\"company\":null," +
                          "\"companyNonPublic\":true," +
                          "\"positionHeld\":null," +
                          "\"positionHeldNonPublic\":true," +
                          "\"updatedOn\":\"date\"" +
                          "}")
          ),

          NevisTestChecker.of(true, 200, JsonParserFactory.getJsonParser().parseMap(
                  "{" +
                          "\"nickNameNonPublic\":true," +
                          "\"firstName\":null," +
                          "\"firstNameNonPublic\":true," +
                          "\"middleName\":null," +
                          "\"middleNameNonPublic\":true," +
                          "\"lastName\":null," +
                          "\"lastNameNonPublic\":true," +
                          "\"gender\":null," +
                          "\"genderNonPublic\":true," +
                          "\"dateOfBirth\":null," +
                          "\"dateOfBirthNonPublic\":true," +
                          "\"country\":null," +
                          "\"countryNonPublic\":true," +
                          "\"city\":null," +
                          "\"cityNonPublic\":true," +
                          "\"company\":null," +
                          "\"companyNonPublic\":true," +
                          "\"positionHeld\":null," +
                          "\"positionHeldNonPublic\":true" +
                          "}")
                  , JsonParserFactory.getJsonParser().parseMap(
                          "{\"nickName\":null," +
                                  "\"nickNameNonPublic\":true," +
                                  "\"firstName\":null," +
                                  "\"firstNameNonPublic\":true," +
                                  "\"middleName\":null," +
                                  "\"middleNameNonPublic\":true," +
                                  "\"lastName\":null," +
                                  "\"lastNameNonPublic\":true," +
                                  "\"gender\":null," +
                                  "\"genderNonPublic\":true," +
                                  "\"dateOfBirth\":null," +
                                  "\"dateOfBirthNonPublic\":true," +
                                  "\"country\":null," +
                                  "\"countryNonPublic\":true," +
                                  "\"city\":null," +
                                  "\"cityNonPublic\":true," +
                                  "\"company\":null," +
                                  "\"companyNonPublic\":true," +
                                  "\"positionHeld\":null," +
                                  "\"positionHeldNonPublic\":true," +
                                  "\"updatedOn\":\"date\"" +
                                  "}")
          ),

          NevisTestChecker.of(true, 200, JsonParserFactory.getJsonParser().parseMap(
                  "{" +
                          "\"nickNameNonPublic\":false," +
                          "\"firstName\":\"12345678901234567890777\"," +
                          "\"firstNameNonPublic\":false," +
                          "\"middleName\":\"12345678901234567890777\"," +
                          "\"middleNameNonPublic\":false," +
                          "\"lastName\":\"12345678901234567890777\"," +
                          "\"lastNameNonPublic\":false," +
                          "\"gender\":\"m\"," +
                          "\"genderNonPublic\":false," +
                          "\"dateOfBirth\":\"2018-07-11\"," +
                          "\"dateOfBirthNonPublic\":false," +
                          "\"country\":\"ru\"," +
                          "\"countryNonPublic\":false," +
                          "\"city\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890777\"," +
                          "\"cityNonPublic\":false," +
                          "\"company\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890777\"," +
                          "\"companyNonPublic\":false," +
                          "\"positionHeld\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890777\"," +
                          "\"positionHeldNonPublic\":false" +
                          "}"),
                  JsonParserFactory.getJsonParser().parseMap(
                          "{\"nickName\":null," +
                                  "\"nickNameNonPublic\":false," +
                                  "\"firstName\":\"12345678901234567890\"," +
                                  "\"firstNameNonPublic\":false," +
                                  "\"middleName\":\"12345678901234567890\"," +
                                  "\"middleNameNonPublic\":false," +
                                  "\"lastName\":\"12345678901234567890\"," +
                                  "\"lastNameNonPublic\":false," +
                                  "\"gender\":\"M\"," +
                                  "\"genderNonPublic\":false," +
                                  "\"dateOfBirth\":\"2018-07-11\"," +
                                  "\"dateOfBirthNonPublic\":false," +
                                  "\"country\":\"RU\"," +
                                  "\"countryNonPublic\":false," +
                                  "\"city\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                                  "\"cityNonPublic\":false," +
                                  "\"company\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                                  "\"companyNonPublic\":false," +
                                  "\"positionHeld\":\"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"," +
                                  "\"positionHeldNonPublic\":false," +
                                  "\"updatedOn\":\"date\"" +
                                  "}")
          )
  );
}