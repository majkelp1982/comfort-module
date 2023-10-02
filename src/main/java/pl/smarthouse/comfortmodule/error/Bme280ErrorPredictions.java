package pl.smarthouse.comfortmodule.error;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleDao;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.smartmonitoring.model.ErrorPrediction;
import pl.smarthouse.smartmonitoring.service.ErrorHandlingService;

@Configuration
@RequiredArgsConstructor
public class Bme280ErrorPredictions {
  // Error messages
  private static final String BME280_TIMEOUT = "BME280 sensor update timeout";

  private static final int UPDATE_TIMEOUT_IN_SECONDS = 120;
  private final ComfortModuleService comfortModuleService;
  private final ErrorHandlingService errorHandlingService;
  Predicate<LocalDateTime> updateTimeout =
      updateTimestamp ->
          Objects.isNull(updateTimestamp)
              || updateTimestamp
                  .plusSeconds(UPDATE_TIMEOUT_IN_SECONDS)
                  .isBefore(LocalDateTime.now());

  @PostConstruct
  public void postConstructor() {
    sensorReadTimeout();
  }

  private void sensorReadTimeout() {
    final Predicate<? extends ModuleDao> bme280Timeout =
        (ComfortModuleDao comfortModuleDao) ->
            updateTimeout.test(comfortModuleDao.getSensorResponse().getResponseUpdate());
    errorHandlingService.add(
        new ErrorPrediction(
            bme280Timeout,
            BME280_TIMEOUT,
            result -> {
              comfortModuleService
                  .getBme280Sensor()
                  .map(
                      bme280ResponseDto -> {
                        bme280ResponseDto.setError(result);
                        return bme280ResponseDto;
                      })
                  .flatMap(comfortModuleService::setBme280Sensor)
                  .subscribe();
            }));
  }
}
