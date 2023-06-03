package pl.smarthouse.comfortmodule.service.functions;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.service.ComfortModuleParamsService;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.comfortmodule.utils.TimeRangeUtils;
import pl.smarthouse.sharedobjects.dto.comfort.core.ForcedAirControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.TemperatureControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRange;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.enums.Operation;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Getter
public class ForcedAirService {
  private final ComfortModuleService comfortModuleService;
  private final ComfortModuleParamsService comfortModuleParamsService;
  private final LocalDateTime humidityOverLimitTimestamp = LocalDateTime.now();
  private Bme280ResponseDto bme280ResponseDto;
  private Operation requiredOperation = Operation.STANDBY;
  private int requiredPower = 0;

  @Scheduled(fixedDelay = 10000)
  private void forcedAirScheduler() {
    comfortModuleService
        .getBme280Sensor()
        .doOnNext(sensor -> bme280ResponseDto = sensor)
        .flatMap(ignore -> comfortModuleParamsService.getParams())
        .map(comfortModuleParamsDto -> comfortModuleParamsDto.getTemperatureControl())
        .doOnNext(this::calculateOperation)
        .subscribe();
  }

  private void calculateOperation(final TemperatureControl temperatureControl) {

    final double requiredTemperature = temperatureControl.getRequiredTemperature();
    final double currentTemperature = bme280ResponseDto.getTemperature();
    final ForcedAirControl forcedAirControl = temperatureControl.getForcedAirControl();
    final double forcedAirTolerance = forcedAirControl.getForcedAirTolerance();
    final double airConditionTolerance = forcedAirControl.getAirConditionTolerance();
    final Set<TimeRange> timeRanges =
        TimeRangeUtils.getTimeRangesByDayOfTheWeek(
            forcedAirControl.getWeekendTimeRanges(), forcedAirControl.getWorkdayTimeRanges());

    Operation resultOperation = Operation.STANDBY;
    int resultRequiredPower = 0;

    if (!forcedAirControl.isForcedAirEnabled() || !TimeRangeUtils.inTimeRange(timeRanges)) {
      requiredOperation = Operation.STANDBY;
      requiredPower = 0;
      return;
    }

    // Calculate operation

    if (currentTemperature > (requiredTemperature + forcedAirTolerance)) {
      resultOperation = Operation.AIR_COOLING;
      resultRequiredPower = forcedAirControl.getForcedAirRequiredPower();
    }

    if ((currentTemperature > (requiredTemperature + airConditionTolerance))) {
      resultOperation = Operation.AIR_CONDITION;
      resultRequiredPower = forcedAirControl.getAirConditionRequiredPower();
    }

    if ((currentTemperature < (requiredTemperature - forcedAirTolerance))) {
      resultOperation = Operation.AIR_HEATING;
      resultRequiredPower = forcedAirControl.getAirConditionRequiredPower();
    }

    if (Math.abs(currentTemperature - forcedAirTolerance) <= 0.5) {
      resultOperation = Operation.STANDBY;
      resultRequiredPower = 0;
    }

    requiredOperation = resultOperation;
    requiredPower = resultRequiredPower;
  }
}
