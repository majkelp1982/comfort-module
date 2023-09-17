package pl.smarthouse.comfortmodule.service.errorhandler;

import java.time.LocalDateTime;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;

@EnableScheduling
@RequiredArgsConstructor
@Service
@Slf4j
public class Bme280ErrorHandlerService {
  private static final int UPDATE_TIMEOUT_IN_SECONDS = 120;
  private final ComfortModuleService comfortModuleService;
  Predicate<LocalDateTime> updateTimeout =
      updateTimestamp ->
          updateTimestamp.plusSeconds(UPDATE_TIMEOUT_IN_SECONDS).isBefore(LocalDateTime.now());

  @Scheduled(fixedDelay = 30000)
  private void noUpdateLongTime() {
    comfortModuleService
        .getBme280Sensor()
        .map(
            bme280ResponseDto -> {
              if (bme280ResponseDto.getResponseUpdate() == null
                  || updateTimeout.test(bme280ResponseDto.getResponseUpdate())) {
                log.error(
                    "BME280 sensor is not updated over {} seconds", UPDATE_TIMEOUT_IN_SECONDS);
                bme280ResponseDto.setError(true);
              }
              return bme280ResponseDto;
            })
        .flatMap(comfortModuleService::setBme280Sensor)
        .subscribe();
  }
}
