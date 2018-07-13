package dwfe.nevis.db.account.phone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "nevis_account_phone")
public class NevisAccountPhone
{
  @Id
  private Long accountId;
  private String value;
  private boolean nonPublic;
  private boolean confirmed;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;


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

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public boolean isNonPublic()
  {
    return nonPublic;
  }

  public void setNonPublic(boolean nonPublic)
  {
    this.nonPublic = nonPublic;
  }

  public boolean isConfirmed()
  {
    return confirmed;
  }

  public void setConfirmed(boolean confirmed)
  {
    this.confirmed = confirmed;
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

    NevisAccountPhone that = (NevisAccountPhone) o;

    return accountId.equals(that.accountId);
  }

  @Override
  public int hashCode()
  {
    return accountId.hashCode();
  }
}
