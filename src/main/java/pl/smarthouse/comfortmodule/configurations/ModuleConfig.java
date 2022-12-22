package pl.smarthouse.comfortmodule.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.enums.ComfortZone;
import pl.smarthouse.smartmodule.model.actors.actor.Actor;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.typelibs.BME280.BME280;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

import javax.annotation.PostConstruct;

@Configuration
@Getter
public class ModuleConfig {
  // Actors
  public static final String BME280 = "bme280";
  // Module specific
  private static final String FIRMWARE = "20221210.21";
  private static final String VERSION = "20221215.21";
  private final pl.smarthouse.smartmodule.model.configuration.Configuration configuration;
  @Autowired ModuleService moduleService;
  @Autowired ManagerService managerService;
  private String macAddress;
  private ComfortZone comfortZone;

  public ModuleConfig(final ApplicationArguments applicationArguments) {
    checkApplicationArguments(applicationArguments);
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            comfortZone.toString(), FIRMWARE, VERSION, macAddress, createActors());
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
    comfortZone = ComfortZone.valueOf(applicationArguments.getSourceArgs()[0]);
    macAddress = applicationArguments.getSourceArgs()[1];
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();
    actorMap.putActor(new BME280(BME280, 4));
    return actorMap;
  }

  public Actor getActor(final String name) {
    return configuration.getActorMap().getActor(name);
  }
}
