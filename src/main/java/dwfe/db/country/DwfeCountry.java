package dwfe.db.country;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dwfe_countries")
public class DwfeCountry
{
  private String country;
  @Id
  private String alpha2;
  private String alpha3;
  private String phoneCode;


  //
  //  GETTERs and SETTERs
  //

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public String getAlpha2()
  {
    return alpha2;
  }

  public void setAlpha2(String alpha2)
  {
    this.alpha2 = alpha2;
  }

  public String getAlpha3()
  {
    return alpha3;
  }

  public void setAlpha3(String alpha3)
  {
    this.alpha3 = alpha3;
  }

  public String getPhoneCode()
  {
    return phoneCode;
  }

  public void setPhoneCode(String phoneCode)
  {
    this.phoneCode = phoneCode;
  }


  //
  //  equals, hashCode
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    var that = (DwfeCountry) o;

    return alpha2.equals(that.alpha2);
  }

  @Override
  public int hashCode()
  {
    return alpha2.hashCode();
  }
}
