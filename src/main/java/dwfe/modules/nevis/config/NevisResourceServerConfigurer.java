package dwfe.modules.nevis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableResourceServer
public class NevisResourceServerConfigurer extends ResourceServerConfigurerAdapter
{
  @Autowired
  private TokenStore tokenStore;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) throws Exception
  {
    resources
            .tokenStore(tokenStore)
    ;
  }

  @Override
  public void configure(HttpSecurity http) throws Exception
  {
    http
            //you don't need protection from CSRF, because RESTful
            .csrf().disable()

            //you don't need sessions, because RESTful
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            //but the session is still created: HttpServletRequest request, request.getSession()

            //for prevent exception: Cannot apply ExpressionUrlAuthorizationConfigurer
            .authorizeRequests().anyRequest().permitAll()
    ;
  }

}
