package dwfe.nevis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.List;

@Configuration
@EnableAuthorizationServer
public class NevisAuthorizationServerConfigurer extends AuthorizationServerConfigurerAdapter
{
  private final AuthenticationManager authenticationManager;
  private final TokenStore tokenStore;
  private final TokenEnhancer tokenEnhancer;
  private final UserDetailsService userDetailsService;
  private final NevisConfigProperties prop;

  @Autowired
  public NevisAuthorizationServerConfigurer(AuthenticationManager authenticationManager, TokenStore tokenStore, TokenEnhancer tokenEnhancer, UserDetailsService userDetailsService, NevisConfigProperties prop)
  {
    this.authenticationManager = authenticationManager;
    this.tokenStore = tokenStore;
    this.tokenEnhancer = tokenEnhancer;
    this.userDetailsService = userDetailsService;
    this.prop = prop;
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints)
  {
    final var tokenEnhancerChain = new TokenEnhancerChain();
    tokenEnhancerChain.setTokenEnhancers(List.of(tokenEnhancer));

    endpoints
            .pathMapping("/oauth/token", prop.getApi() + prop.getResource().getSignIn())
            .authenticationManager(authenticationManager)
            .tokenStore(tokenStore)
            .tokenEnhancer(tokenEnhancerChain)
            .userDetailsService(userDetailsService) //needed for token refreshing
    ;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security)
  {
    //Client credentials is not encrypted
    security.passwordEncoder(NoOpPasswordEncoder.getInstance());
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer configurer) throws Exception
  {
    //Authorization Server: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html#boot-features-security-oauth2-authorization-server
    //Access Token Request: https://tools.ietf.org/html/rfc6749#section-4.3.2

    configurer
            .inMemory() // in Memory or in JDBC

            .withClient(prop.getoAuth2ClientUnlimited().getId())
            .secret(prop.getoAuth2ClientUnlimited().getPassword())
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(0)

            .and()

            .withClient(prop.getOauth2ClientTrusted().getId())
            .secret(prop.getOauth2ClientTrusted().getPassword())
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(prop.getOauth2ClientTrusted().getTokenValiditySeconds())

            .and()

            .withClient(prop.getOauth2ClientUntrusted().getId())
            .secret(prop.getOauth2ClientUntrusted().getPassword())
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(prop.getOauth2ClientUntrusted().getTokenValiditySeconds())
    ;
  }
}
