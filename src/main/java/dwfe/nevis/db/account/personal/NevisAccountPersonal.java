package dwfe.nevis.db.account.personal;

import dwfe.nevis.db.other.gender.NevisGender;
import dwfe.nevis.util.NevisUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static dwfe.nevis.util.NevisUtil.cutStr;

@Entity
@Table(name = "nevis_account_personal")
public class NevisAccountPersonal
{
  @Id
  private Long accountId;

  private String nickName;
  private Boolean nickNameNonPublic;

  private String firstName;
  private Boolean firstNameNonPublic;

  private String middleName;
  private Boolean middleNameNonPublic;

  private String lastName;
  private Boolean lastNameNonPublic;

  private NevisGender gender;
  private Boolean genderNonPublic;

  private LocalDate dateOfBirth;
  private Boolean dateOfBirthNonPublic;

  private String country;
  private Boolean countryNonPublic;

  private String city;
  private Boolean cityNonPublic;

  private String company;
  private Boolean companyNonPublic;

  private String positionHeld;
  private Boolean positionHeldNonPublic;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;


  public static NevisAccountPersonal of(String nickName, boolean nickNameNonPublic,
                                        String firstName, boolean firstNameNonPublic,
                                        String middleName, boolean middleNameNonPublic,
                                        String lastName, boolean lastNameNonPublic,
                                        NevisGender gender, boolean genderNonPublic,
                                        LocalDate dateOfBirth, boolean dateOfBirthNonPublic,
                                        String country, boolean countryNonPublic,
                                        String city, boolean cityNonPublic,
                                        String company, boolean companyNonPublic,
                                        String positionHeld, boolean positionHeldNonPublic)
  {
    var profilePersonal = new NevisAccountPersonal();
    profilePersonal.nickName = nickName;
    profilePersonal.nickNameNonPublic = nickNameNonPublic;
    profilePersonal.firstName = firstName;
    profilePersonal.firstNameNonPublic = firstNameNonPublic;
    profilePersonal.middleName = middleName;
    profilePersonal.middleNameNonPublic = middleNameNonPublic;
    profilePersonal.lastName = lastName;
    profilePersonal.lastNameNonPublic = lastNameNonPublic;
    profilePersonal.gender = gender;
    profilePersonal.genderNonPublic = genderNonPublic;
    profilePersonal.dateOfBirth = dateOfBirth;
    profilePersonal.dateOfBirthNonPublic = dateOfBirthNonPublic;
    profilePersonal.country = country;
    profilePersonal.countryNonPublic = countryNonPublic;
    profilePersonal.city = city;
    profilePersonal.cityNonPublic = cityNonPublic;
    profilePersonal.company = company;
    profilePersonal.companyNonPublic = companyNonPublic;
    profilePersonal.positionHeld = positionHeld;
    profilePersonal.positionHeldNonPublic = positionHeldNonPublic;
    profilePersonal.updatedOn = LocalDateTime.now();
    return profilePersonal;
  }

  //
  //  GETTERs and SETTERs
  //

  public Long getAccountId()
  {
    return accountId;
  }

  public void setAccountId(Long accountId)
  {
    this.accountId = accountId;
  }

  public String getNickName()
  {
    return nickName;
  }

  public void setNickName(String nickName)
  {
    this.nickName = nickName;
  }

  public Boolean getNickNameNonPublic()
  {
    return nickNameNonPublic;
  }

  public void setNickNameNonPublic(Boolean nickNameNonPublic)
  {
    this.nickNameNonPublic = nickNameNonPublic;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = cutStr(firstName, 20);
  }

  public Boolean getFirstNameNonPublic()
  {
    return firstNameNonPublic;
  }

  public void setFirstNameNonPublic(Boolean firstNameNonPublic)
  {
    this.firstNameNonPublic = firstNameNonPublic;
  }

  public String getMiddleName()
  {
    return middleName;
  }

  public void setMiddleName(String middleName)
  {
    this.middleName = cutStr(middleName, 20);
  }

  public Boolean getMiddleNameNonPublic()
  {
    return middleNameNonPublic;
  }

  public void setMiddleNameNonPublic(Boolean middleNameNonPublic)
  {
    this.middleNameNonPublic = middleNameNonPublic;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = cutStr(lastName, 20);
  }

  public Boolean getLastNameNonPublic()
  {
    return lastNameNonPublic;
  }

  public void setLastNameNonPublic(Boolean lastNameNonPublic)
  {
    this.lastNameNonPublic = lastNameNonPublic;
  }

  public NevisGender getGender()
  {
    return gender;
  }

  public void setGender(NevisGender gender)
  {
    this.gender = gender;
  }

  public Boolean getGenderNonPublic()
  {
    return genderNonPublic;
  }

  public void setGenderNonPublic(Boolean genderNonPublic)
  {
    this.genderNonPublic = genderNonPublic;
  }

  public LocalDate getDateOfBirth()
  {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth)
  {
    this.dateOfBirth = dateOfBirth;
  }

  public Boolean getDateOfBirthNonPublic()
  {
    return dateOfBirthNonPublic;
  }

  public void setDateOfBirthNonPublic(Boolean dateOfBirthNonPublic)
  {
    this.dateOfBirthNonPublic = dateOfBirthNonPublic;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = NevisUtil.strToUpperCase(country);
  }

  public Boolean getCountryNonPublic()
  {
    return countryNonPublic;
  }

  public void setCountryNonPublic(Boolean countryNonPublic)
  {
    this.countryNonPublic = countryNonPublic;
  }

  public String getCity()
  {
    return city;
  }

  public void setCity(String city)
  {
    this.city = cutStr(city, 100);
  }

  public Boolean getCityNonPublic()
  {
    return cityNonPublic;
  }

  public void setCityNonPublic(Boolean cityNonPublic)
  {
    this.cityNonPublic = cityNonPublic;
  }

  public String getCompany()
  {
    return company;
  }

  public void setCompany(String company)
  {
    this.company = cutStr(company, 100);
  }

  public Boolean getCompanyNonPublic()
  {
    return companyNonPublic;
  }

  public void setCompanyNonPublic(Boolean companyNonPublic)
  {
    this.companyNonPublic = companyNonPublic;
  }

  public String getPositionHeld()
  {
    return positionHeld;
  }

  public void setPositionHeld(String positionHeld)
  {
    this.positionHeld = cutStr(positionHeld, 100);
  }

  public Boolean getPositionHeldNonPublic()
  {
    return positionHeldNonPublic;
  }

  public void setPositionHeldNonPublic(Boolean positionHeldNonPublic)
  {
    this.positionHeldNonPublic = positionHeldNonPublic;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }

  public void setUpdatedOn(LocalDateTime updatedOn)
  {
    this.updatedOn = updatedOn;
  }


  //
  //  equals, hashCode
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    var that = (NevisAccountPersonal) o;

    return accountId.equals(that.accountId);
  }

  @Override
  public int hashCode()
  {
    return accountId.hashCode();
  }
}
