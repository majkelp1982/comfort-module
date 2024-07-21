package pl.smarthouse.comfortmodule.service.functions;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.service.ComfortModuleParamsService;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.comfort.core.HumidityAlert;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.enums.Operation;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Getter
public class HumidityService {
  private final ComfortModuleService comfortModuleService;
  private final ComfortModuleParamsService comfortModuleParamsService;
  private Bme280ResponseDto bme280ResponseDto;
  private Operation requiredOperation = Operation.STANDBY;
  private int requiredPower = 0;
  private long leftHoldTimeInMinutes = 0;
  private LocalDateTime humidityOverLimitTimestamp = LocalDateTime.now();

  public Mono<HumidityAlert> scheduler() {
    return comfortModuleService
        .getBme280SensorDto()
        .doOnNext(sensor -> bme280ResponseDto = sensor)
        .map(ignore -> comfortModuleParamsService.getParams())
        .map(ComfortModuleParamsDto::getHumidityAlert)
        .doOnNext(this::calculateOperation);
  }

  private void calculateOperation(final HumidityAlert humidityAlert) {
    if (!humidityAlert.isEnabled()) {
      requiredOperation = Operation.STANDBY;
      requiredPower = 0;
      leftHoldTimeInMinutes = 0;
      return;
    }

    // Calculate operation
    if (bme280ResponseDto.getHumidity() > humidityAlert.getMaxHumidity()) {
      requiredOperation = Operation.HUMIDITY_ALERT;
      humidityOverLimitTimestamp = LocalDateTime.now();
    }

    leftHoldTimeInMinutes =
        humidityAlert.getHoldTimeInMinutes()
            - Duration.between(humidityOverLimitTimestamp, LocalDateTime.now()).toMinutes();

    if (leftHoldTimeInMinutes <= 0 || Operation.STANDBY.equals(requiredOperation)) {
      requiredOperation = Operation.STANDBY;
      requiredPower = 0;
      leftHoldTimeInMinutes = 0;
    }

    // Calculate required power
    if (bme280ResponseDto.getHumidity() >= (humidityAlert.getMaxHumidity() + 10)) {
      requiredPower = humidityAlert.getRequiredTurboPower();
    } else {
      requiredPower = humidityAlert.getRequiredPower();
    }
  }
}
