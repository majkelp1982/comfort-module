package pl.smarthouse.comfortmodule.error;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.properties.ActorProperties;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.smartmodule.utils.errorpredictions.Bme280ErrorPredictionsUtils;
import pl.smarthouse.smartmonitoring.service.ErrorHandlingService;

@Configuration
@RequiredArgsConstructor
public class Bme280ErrorPredictions {

  private final ComfortModuleService comfortModuleService;
  private final ErrorHandlingService errorHandlingService;

  @PostConstruct
  public void postConstructor() throws Exception {
    Bme280ErrorPredictionsUtils.setBme280SensorErrorPredictions(
        errorHandlingService,
        ActorProperties.BME280,
        comfortModuleService::getBme280SensorResponse);
  }
}
