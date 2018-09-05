Java10 / Spring-Boot2 / OAuth2 / MySQL / RESTful 
<br><br>

  * [Lifecycle](#lifecycle-of-dwfe-app)
  * [DWFE](#dwfe)
    * [Common](#dwfe-common)
  * [Nevis module](#nevis-module)
    * [Common](#account-common)
    * [Access](#account-access)
    * [Email](#account-email)
    * [Phone](#account-phone)
    * [Personal](#account-personal)
    * [Third-party Auth](#account-third-party-auth)
  * [Management](#management)
    * [Properties](#properties)
    * [Production Build Run](#production-build-run)
  * [Other](#other)
    * [Errors](#errors)
    * [Time Zone](#time-zone)
    * [Email alerts](#email-alerts)
  
# Lifecycle of DWFE App
![Lifecycle of DWFE App](./assets/img-readme/lifecycle-of-dwfe-app.png)

# DWFE
## DWFE Common
![DWFE Common](./assets/img-readme/dwfe-common.png)

# Nevis module
## Account Common
![Account Common](./assets/img-readme/account-common.png)
## Account Access
![Account Access](./assets/img-readme/account-access.png)
## Account Email
![Account Email](./assets/img-readme/account-email.png)
## Account Phone
![Account Phone](./assets/img-readme/account-phone.png)
## Account Personal
![Account Personal](./assets/img-readme/account-personal.png)
## Account Third-party Auth
![Account Third-party Auth](./assets/img-readme/account-third-party-auth.png)

# Management
## Properties
![Properties](./assets/img-readme/properties.png)
## Production Build Run
![Production Build Run](./assets/img-readme/production-build-run.png)

# Other
## Errors
![Errors](./assets/img-readme/errors.png)
<br>
The list of OAuth2 server error-codes and their mapping see [here](./assets/error-mapping/oauth2-server-error-mapping.js) (may not include unknown errors to me).
<br>
Also you should know that under one error-code, there may be several different error_description.
<br>
For example `invalid_grant` error-code can be returned with the following error_descriptions:<br>
   * *Bad credentials* - if login or/and password is incorrect
   * *User is disabled* - if enabled field set to false
   * *User account is locked* - if account_non_locked field set to false
   * *User credentials have expired* - if credentials_non_expired field set to false
   * *User account has expired* - if account_non_expired field set to false
  
In this regard, it may be worthwhile instead of mapping error-codes to output error_description.
<br><br>
Also the list of DWFE App error-codes and their mapping see [here](./assets/error-mapping/dwfe-app-error-mapping.js).

## Time Zone
![Time Zone](./assets/img-readme/time-zone.png)
## Email alerts
![Email alerts](./assets/img-readme/email-alerts.png)
<br><br>
Preparation of mail templates was made in a separate project [backend-letter-templates](https://gitlab.com/nevis-proj/backend-letter-templates).
<br>
To fill the mail templates used [Thymeleaf](http://www.thymeleaf.org) template engine.

