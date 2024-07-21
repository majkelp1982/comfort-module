package pl.smarthouse.comfortmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComfortModuleApplication {
  public static void main(final String[] args) {
    SpringApplication.run(ComfortModuleApplication.class, args);
  }
}
