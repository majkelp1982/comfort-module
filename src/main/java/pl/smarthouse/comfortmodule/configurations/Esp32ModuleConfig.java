package pl.smarthouse.comfortmodule.configurations;

import static pl.smarthouse.comfortmodule.properties.ActorProperties.BME280;
import static pl.smarthouse.comfortmodule.properties.ActorProperties.BME280_PIN;
import static pl.smarthouse.comfortmodule.properties.Esp32ModuleProperties.FIRMWARE;
import static pl.smarthouse.comfortmodule.properties.Esp32ModuleProperties.VERSION;

import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.enums.ComfortZone;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

@Configuration
@Getter
public class Esp32ModuleConfig {
  // Module specific
  private final pl.smarthouse.smartmodule.model.configuration.Configuration configuration;
  @Autowired ModuleService moduleService;
  @Autowired ManagerService managerService;
  ComfortZone comfortZone;
  private String macAddress;

  public Esp32ModuleConfig(final ApplicationArguments applicationArguments) {
    checkApplicationArguments(applicationArguments);
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            comfortZone.getComfortZoneName(), FIRMWARE, VERSION, macAddress, createActors());
  }

  @PostConstruct
  public void postConstruct() {
    moduleService.setConfiguration(configuration);
    managerService.setConfiguration(configuration);
  }

  private void checkApplicationArguments(final ApplicationArguments applicationArguments) {
    if (applicationArguments.getSourceArgs().length != 2) {
      throw new IllegalArgumentException(
          "Should be 2 arguments. First module type, second mac address");
    }
    final ZoneName zoneName = ZoneName.valueOf(applicationArguments.getSourceArgs()[0]);
    comfortZone = new ComfortZone(zoneName);
    macAddress = applicationArguments.getSourceArgs()[1];
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();
    actorMap.putActor(new Bme280(BME280, BME280_PIN));
    return actorMap;
  }
}
