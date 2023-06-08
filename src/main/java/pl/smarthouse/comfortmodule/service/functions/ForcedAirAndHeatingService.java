package pl.smarthouse.comfortmodule.service.functions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Setter
public class ForcedAirAndHeatingService {
  private final ComfortModuleService comfortModuleService;
  private final ComfortModuleParamsService comfortModuleParamsService;
  private final LocalDateTime humidityOverLimitTimestamp = LocalDateTime.now();
  private Bme280ResponseDto bme280ResponseDto = new Bme280ResponseDto();
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

  public void calculateOperation(final TemperatureControl temperatureControl) {
    // delta temperature
    // + -> to hot
    // - -> to cold
    final BigDecimal currentTemperature =
        new BigDecimal(String.valueOf(bme280ResponseDto.getTemperature()));
    final BigDecimal requiredTemperature =
        new BigDecimal(String.valueOf(temperatureControl.getRequiredTemperature()));
    final double deltaTemp = currentTemperature.subtract(requiredTemperature).doubleValue();

    final ForcedAirControl forcedAirControl = temperatureControl.getForcedAirControl();
    final double forcedAirTolerance = forcedAirControl.getForcedAirTolerance();
    final double airConditionTolerance = forcedAirControl.getAirConditionTolerance();

    final double floorHeatingTolerance = temperatureControl.getHeatingControl().getLowTolerance();

    Operation resultOperation = (requiredOperation == null) ? Operation.STANDBY : requiredOperation;

    // Calculate operation
    // Heating
    if (deltaTemp < -floorHeatingTolerance
        || (Operation.AIR_HEATING.equals(resultOperation)
            && deltaTemp >= -forcedAirTolerance + 0.5)) {
      resultOperation = Operation.FLOOR_HEATING;
    }

    if (deltaTemp < -forcedAirTolerance) {
      resultOperation = Operation.AIR_HEATING;
    }

    if (Operation.FLOOR_HEATING.equals(resultOperation) && deltaTemp >= 0.0) {
      resultOperation = Operation.STANDBY;
    }

    // Air Cooling and Air condition
    if (deltaTemp > forcedAirTolerance) {
      resultOperation = Operation.AIR_COOLING;
    }

    if (deltaTemp > airConditionTolerance) {
      resultOperation = Operation.AIR_CONDITION;
    }

    if (Operation.AIR_COOLING.equals(resultOperation) && deltaTemp <= 0.0) {
      resultOperation = Operation.STANDBY;
    }

    resultOperation = disableIfFunctionNotActive(temperatureControl, resultOperation);
    resultOperation =
        disableIfOutOfTimeRanges(
            resultOperation,
            TimeRangeUtils.getTimeRangesByDayOfTheWeek(
                forcedAirControl.getWeekendTimeRanges(), forcedAirControl.getWorkdayTimeRanges()),
            TimeRangeUtils.getTimeRangesByDayOfTheWeek(
                temperatureControl.getHeatingControl().getWeekendTimeRanges(),
                temperatureControl.getHeatingControl().getWorkdayTimeRanges()));

    if (bme280ResponseDto.isError()) {
      resultOperation = Operation.STANDBY;
    }

    requiredOperation = resultOperation;
    switch (resultOperation) {
      case STANDBY:
      case FLOOR_HEATING:
        requiredPower = 0;
        break;
      case AIR_HEATING:
      case AIR_COOLING:
        requiredPower = forcedAirControl.getForcedAirRequiredPower();
        break;
      case AIR_CONDITION:
        requiredPower = forcedAirControl.getAirConditionRequiredPower();
        break;
    }
  }

  private Operation disableIfFunctionNotActive(
      final TemperatureControl temperatureControl, final Operation resultOperation) {
    if (!temperatureControl.getHeatingControl().isHeatingEnabled()) {
      if (Operation.FLOOR_HEATING.equals(resultOperation)) {
        return Operation.STANDBY;
      }
    }
    if (!temperatureControl.getForcedAirControl().isAirConditionEnabled()) {
      if (Operation.AIR_CONDITION.equals(resultOperation)) {
        return temperatureControl.getForcedAirControl().isForcedAirEnabled()
            ? Operation.AIR_COOLING
            : Operation.STANDBY;
      }
    }

    if (!temperatureControl.getForcedAirControl().isForcedAirEnabled()) {
      if (List.of(Operation.AIR_HEATING, Operation.AIR_COOLING).contains(resultOperation)) {
        return Operation.STANDBY;
      }
    }
    if (!temperatureControl.getForcedAirControl().isAirConditionEnabled()) {
      if (Operation.AIR_CONDITION.equals(resultOperation)) {
        return Operation.STANDBY;
      }
    }
    return resultOperation;
  }

  private Operation disableIfOutOfTimeRanges(
      final Operation resultOperation,
      final Set<TimeRange> forcedAirTimeRanges,
      final Set<TimeRange> floorHeatingTimeRanges) {
    if (Operation.FLOOR_HEATING.equals(resultOperation)) {
      if (!TimeRangeUtils.inTimeRange(floorHeatingTimeRanges)) {
        return Operation.STANDBY;
      }
    }
    if (List.of(Operation.AIR_HEATING, Operation.AIR_COOLING, Operation.AIR_CONDITION)
        .contains(resultOperation)) {
      if (!TimeRangeUtils.inTimeRange(forcedAirTimeRanges)) {
        return Operation.STANDBY;
      }
    }

    return resultOperation;
  }
}
