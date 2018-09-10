package dwfe.modules.nevis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class NevisWebSecurityConfigurer extends WebSecurityConfigurerAdapter
{
  private final UserDetailsService userDetailsService;

  @Autowired
  public NevisWebSecurityConfigurer(UserDetailsService userDetailsService)
  {
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception
  {
    auth
            .userDetailsService(userDetailsService)   // checking the existence of an incoming username
            .passwordEncoder(bcrypt());               // checking raw password with its hash in the database
  }

  @Bean
  @Primary
  @Override
  protected AuthenticationManager authenticationManager() throws Exception
  {
    return super.authenticationManager();
  }

  private DelegatingPasswordEncoder bcrypt()
  {
    // == https://docs.spring.io/spring-security/site/docs/current/reference/html/core-services.html#pe-dpe
    var encoders = new HashMap<String, PasswordEncoder>();
    var idForEncode = "bcrypt";
    encoders.put("bcrypt", new BCryptPasswordEncoder(10));

    return new DelegatingPasswordEncoder(idForEncode, encoders);
  }
}
