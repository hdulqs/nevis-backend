package dwfe.db.mailing;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static dwfe.util.DwfeUtil.formatDateTimeToUTCstring;
import static dwfe.util.DwfeUtil.getJsonFieldFromObj;

@Entity
@IdClass(DwfeMailing.NevisMailingId.class)
@Table(name = "dwfe_mailing")
public class DwfeMailing implements Comparable<DwfeMailing>
{
  @Id
  @Column(updatable = false, insertable = false)
  private LocalDateTime createdOn;

  @Id
  @Enumerated(EnumType.STRING)
  private DwfeMailingType type;

  @Id
  private String email;

  private String data;
  private volatile boolean sent;
  private volatile boolean maxAttemptsReached;

  private String causeOfLastFailure;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;

  @Transient
  private AtomicInteger attempt = new AtomicInteger(0);

  public static DwfeMailing of(DwfeMailingType type, String email, String data)
  {
    var mailing = new DwfeMailing();
    mailing.setCreatedOn(LocalDateTime.now());
    mailing.setType(type);
    mailing.setEmail(email);
    mailing.setData(data);
    mailing.setSent(false);
    mailing.setMaxAttemptsReached(false);
    return mailing;
  }

  public static DwfeMailing of(DwfeMailingType type, String email)
  {
    return of(type, email, "");
  }

  public void clear()
  {
    data = "";
  }


  //
  //  GETTERs and SETTERs
  //


  public LocalDateTime getCreatedOn()
  {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn)
  {
    this.createdOn = createdOn;
  }

  public DwfeMailingType getType()
  {
    return type;
  }

  public void setType(DwfeMailingType type)
  {
    this.type = type;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getData()
  {
    return data;
  }

  public void setData(String data)
  {
    this.data = data;
  }

  public boolean isSent()
  {
    return sent;
  }

  public void setSent(boolean sent)
  {
    this.sent = sent;
  }

  public boolean isMaxAttemptsReached()
  {
    return maxAttemptsReached;
  }

  public void setMaxAttemptsReached(boolean maxAttemptsReached)
  {
    this.maxAttemptsReached = maxAttemptsReached;
  }

  public String getCauseOfLastFailure()
  {
    return causeOfLastFailure;
  }

  public void setCauseOfLastFailure(String causeOfLastFailure)
  {
    this.causeOfLastFailure = causeOfLastFailure;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }

  public AtomicInteger getAttempt()
  {
    return attempt;
  }


  //
  //  equals, hashCode, compareTo, toString
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DwfeMailing that = (DwfeMailing) o;

    if (!createdOn.equals(that.createdOn)) return false;
    if (type != that.type) return false;
    return email.equals(that.email);
  }

  @Override
  public int hashCode()
  {
    int result = createdOn.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + email.hashCode();
    return result;
  }

  @Override
  public int compareTo(DwfeMailing o)
  {
    var result = 0;
    if ((result = createdOn.compareTo(o.createdOn)) == 0)
      if ((result = type.compareTo(o.type)) == 0)
        result = email.compareTo(o.email);
    return result;
  }

  @Override
  public String toString()
  {
    return "{\n" +
            " \"createdOn\": " + "\"" + formatDateTimeToUTCstring(createdOn) + "\",\n" +
            " " + getJsonFieldFromObj("type", type) + ",\n" +
            " " + getJsonFieldFromObj("email", email) + ",\n" +
            " \"data\": \"****\",\n" +
            " \"sent\": " + sent + ",\n" +
            " \"maxAttemptsReached\": " + maxAttemptsReached + ",\n" +
            " " + getJsonFieldFromObj("causeOfLastFailure", causeOfLastFailure) + ",\n" +
            " \"updatedOn\": " + "\"" + formatDateTimeToUTCstring(updatedOn) + "\"\n" +
            "}";
  }

  //
  // OTHER
  //

  public static class NevisMailingId implements Serializable
  {
    private LocalDateTime createdOn;
    private DwfeMailingType type;
    private String email;

    @Override
    public boolean equals(Object o)
    {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      NevisMailingId that = (NevisMailingId) o;

      if (!createdOn.equals(that.createdOn)) return false;
      if (type != that.type) return false;
      return email.equals(that.email);
    }

    @Override
    public int hashCode()
    {
      int result = createdOn.hashCode();
      result = 31 * result + type.hashCode();
      result = 31 * result + email.hashCode();
      return result;
    }
  }
}
