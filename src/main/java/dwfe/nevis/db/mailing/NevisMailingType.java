package dwfe.nevis.db.mailing;

public enum NevisMailingType
{
  //
  // ATTENTION.
  // If one of the enumerations is already in use in database table entries,
  // then CHANGING THE ORDER may cause the logic to malfunction
  //

  WELCOME_ONLY,
  WELCOME_PASSWORD,
  PASSWORD_WAS_CHANGED,
  PASSWORD_RESET_CONFIRM,
  EMAIL_CONFIRM,
}
