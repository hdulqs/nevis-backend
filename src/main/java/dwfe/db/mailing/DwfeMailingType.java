package dwfe.db.mailing;

public enum DwfeMailingType
{
  //
  // ATTENTION.
  // After you make the changes, be sure to update the ENUM field in the database schema
  //

  WELCOME_ONLY,
  WELCOME_PASSWORD,
  PASSWORD_WAS_CHANGED,
  PASSWORD_RESET_CONFIRM,
  EMAIL_CONFIRM,
}
