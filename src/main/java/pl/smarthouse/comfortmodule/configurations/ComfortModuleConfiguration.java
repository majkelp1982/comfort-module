package pl.smarthouse.comfortmodule.configurations;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleDao;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmonitoring.properties.bme280.Bme280DefaultProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;
import pl.smarthouse.smartmonitoring.service.MonitoringService;

@Configuration
@RequiredArgsConstructor
@Getter
public class ComfortModuleConfiguration {
  private final CompareProcessor compareProcessor;
  private final MonitoringService monitoringService;
  private final Esp32ModuleConfig esp32ModuleConfig;
  private ComfortModuleDao comfortModuleDao;

  @PostConstruct
  void postConstruct() {
    final Bme280Response sensor = new Bme280Response();
    sensor.setError(true);
    comfortModuleDao =
        ComfortModuleDao.builder()
            .moduleName(esp32ModuleConfig.getComfortZone().getComfortZoneName())
            .sensorResponse(sensor)
            .build();
    monitoringService.setModuleDaoObject(comfortModuleDao);

    Bme280DefaultProperties.setDefaultProperties(compareProcessor, "sensorResponse");
  }
}
