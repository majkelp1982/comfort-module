package pl.smarthouse.comfortmodule.configurations;

import static pl.smarthouse.comfortmodule.properties.ActorProperties.BME280;
import static pl.smarthouse.comfortmodule.properties.ActorProperties.BME280_PIN;
import static pl.smarthouse.comfortmodule.properties.Esp32ModuleProperties.FIRMWARE;
import static pl.smarthouse.comfortmodule.properties.Esp32ModuleProperties.VERSION;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.enums.ComfortZone;
import pl.smarthouse.comfortmodule.properties.Esp32ModuleProperties;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

@Configuration
@RequiredArgsConstructor
@Getter
public class Esp32ModuleConfig {
  private final ModuleService moduleService;
  private final ManagerService managerService;
  private final Esp32ModuleProperties esp32ModuleProperties;
  ComfortZone comfortZone;
  // Module specific
  private pl.smarthouse.smartmodule.model.configuration.Configuration configuration;
  private String macAddress;

  @PostConstruct
  public void postConstruct() {
    checkApplicationArguments();
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            comfortZone.getComfortZoneName(), FIRMWARE, VERSION, macAddress, createActors());
    moduleService.setConfiguration(configuration);
    managerService.setConfiguration(configuration);
  }

  private void checkApplicationArguments() {
    if ("DEFAULT".equals(esp32ModuleProperties.getZoneName())
        || "UNKNOWN".equals(esp32ModuleProperties.getMacAddress())) {
      throw new IllegalArgumentException(
          "Should be 2 application arguments. First zone name, second mac address");
    }
    final ZoneName zoneName = ZoneName.valueOf(esp32ModuleProperties.getZoneName());
    comfortZone = new ComfortZone(zoneName);
    macAddress = esp32ModuleProperties.getMacAddress();
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();
    actorMap.putActor(new Bme280(BME280, BME280_PIN));
    return actorMap;
  }
}
