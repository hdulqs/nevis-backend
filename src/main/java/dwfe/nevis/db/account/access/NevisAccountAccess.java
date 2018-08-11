package dwfe.nevis.db.account.access;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dwfe.nevis.db.account.authority.NevisAuthority;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "nevis_account_access")
public class NevisAccountAccess implements UserDetails, CredentialsContainer
{
  @Transient
  private String username;
  @Transient
  private NevisAccountUsernameType usernameType;


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  private NevisAccountThirdParty thirdParty;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "nevis_account_authority",
          joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "authority", referencedColumnName = "authority"))
  private Set<NevisAuthority> authorities;

  private boolean accountNonExpired;
  private boolean credentialsNonExpired;
  private boolean accountNonLocked;
  private boolean enabled;

  @Column(updatable = false, insertable = false)
  private LocalDateTime createdOn;

  @Column(updatable = false, insertable = false)
  private LocalDateTime updatedOn;

  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;


  //
  //  IMPLEMENTATION of interfaces
  //

  @JsonIgnore
  @Override
  public String getUsername()
  {
    return username;
  }

  @JsonIgnore
  @Override
  public String getPassword()
  {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities()
  {
    return authorities;
  }

  @Override
  public void eraseCredentials()
  {
    password = "";
  }

  @Override
  public boolean isAccountNonExpired()
  {
    return accountNonExpired;
  }

  @Override
  public boolean isCredentialsNonExpired()
  {
    return credentialsNonExpired;
  }

  @Override
  public boolean isAccountNonLocked()
  {
    return accountNonLocked;
  }

  @Override
  public boolean isEnabled()
  {
    return enabled;
  }


  //
  //  GETTERs and SETTERs
  //

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public NevisAccountThirdParty getThirdParty()
  {
    return thirdParty;
  }

  public void setThirdParty(NevisAccountThirdParty thirdParty)
  {
    this.thirdParty = thirdParty;
  }

  public NevisAccountUsernameType getUsernameType()
  {
    return usernameType;
  }

  void setUsernameType(NevisAccountUsernameType usernameType)
  {
    this.usernameType = usernameType;
  }

  public void setAuthorities(Set<NevisAuthority> authorities)
  {
    this.authorities = authorities;
  }

  public void setAccountNonExpired(boolean accountNonExpired)
  {
    this.accountNonExpired = accountNonExpired;
  }

  public void setCredentialsNonExpired(boolean credentialsNonExpired)
  {
    this.credentialsNonExpired = credentialsNonExpired;
  }

  public void setAccountNonLocked(boolean accountNonLocked)
  {
    this.accountNonLocked = accountNonLocked;
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  public LocalDateTime getCreatedOn()
  {
    return createdOn;
  }

  public LocalDateTime getUpdatedOn()
  {
    return updatedOn;
  }


  //
  //  equals, hashCode
  //

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    var access = (NevisAccountAccess) o;

    return id.equals(access.id);
  }

  @Override
  public int hashCode()
  {
    return id.hashCode();
  }
}
