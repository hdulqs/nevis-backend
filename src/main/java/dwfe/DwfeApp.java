package dwfe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@PropertySource("classpath:application.properties")
public class DwfeApp
{
  @Value("${spring.jpa.properties.hibernate.jdbc.time_zone}")
  private String timeZone;

  @PostConstruct
  void started()
  {
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
  }

  public static void main(String[] args)
  {
    SpringApplication.run(DwfeApp.class, args);
  }
}
