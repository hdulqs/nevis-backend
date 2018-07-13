package dwfe.nevis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@PropertySource("classpath:application.properties")
public class NevisApp
{
  private final Environment env;

  @Autowired
  public NevisApp(Environment env)
  {
    this.env = env;
  }

  @PostConstruct
  void started()
  {
    var time_zone = env.getProperty("spring.jpa.properties.hibernate.jdbc.time_zone");
    TimeZone.setDefault(TimeZone.getTimeZone(time_zone));
  }

  public static void main(String[] args)
  {
    SpringApplication.run(NevisApp.class, args);
  }
}
