package pl.smarthouse.comfortmodule.configurations;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleDao;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Configuration
@Getter
public class ComfortModuleConfiguration {
  private final ComfortModuleDao comfortModuleDao;

  public ComfortModuleConfiguration() {
    final Bme280Response sensor = new Bme280Response();
    sensor.setError(true);
    comfortModuleDao = ComfortModuleDao.builder().sensorResponse(sensor).build();
  }
}
