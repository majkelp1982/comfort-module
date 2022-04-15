package pl.smarthouse.weathermodule.configurations;

import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.model.actors.Actor;
import pl.smarthouse.smartmodule.model.actors.ActorMap;
import pl.smarthouse.smartmodule.model.actors.BME280.BME280;
import pl.smarthouse.smartmodule.model.actors.SDS011.SDS011;
import pl.smarthouse.smartmodule.model.types.ModuleType;

@Configuration
public class ModuleConfig {
  // Actors
  public static final String BME280 = "bme280";
  public static final String SDS011 = "sds011";
  // Module specific
  private static final String VERSION = "2022-04-09.00";
  private static final String MAC_ADDRESS = "XX:XX:BF:4D:77:C8";

  pl.smarthouse.smartmodule.model.configuration.Configuration configuration;

  public ModuleConfig() {
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            ModuleType.WEATHER, VERSION, MAC_ADDRESS, createActors());
  }

  public pl.smarthouse.smartmodule.model.configuration.Configuration getConfiguration() {
    return configuration;
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();
    actorMap.putActor(new BME280(BME280));
    actorMap.putActor(new SDS011(SDS011));
    return actorMap;
  }

  public Actor getActor(final String name) {
    return configuration.getActorMap().getActor(name);
  }
}
