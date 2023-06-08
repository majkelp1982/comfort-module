package pl.smarthouse.comfortmodule.service.functions;

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
    final double deltaTemp =
        bme280ResponseDto.getTemperature() - temperatureControl.getRequiredTemperature();

    final ForcedAirControl forcedAirControl = temperatureControl.getForcedAirControl();
    final boolean forcedAirEnabled = forcedAirControl.isForcedAirEnabled();
    final boolean airConditionEnabled = forcedAirControl.isAirConditionEnabled();
    final double forcedAirTolerance = forcedAirControl.getForcedAirTolerance();
    final double airConditionTolerance = forcedAirControl.getAirConditionTolerance();
    final Set<TimeRange> forcedAirTimeRanges =
        TimeRangeUtils.getTimeRangesByDayOfTheWeek(
            forcedAirControl.getWeekendTimeRanges(), forcedAirControl.getWorkdayTimeRanges());

    final boolean floorHeatingEnabled = temperatureControl.getHeatingControl().isHeatingEnabled();
    final double floorHeatingTolerance = temperatureControl.getHeatingControl().getLowTolerance();
    final Set<TimeRange> floorHeatingTimeRanges =
        TimeRangeUtils.getTimeRangesByDayOfTheWeek(
            temperatureControl.getHeatingControl().getWeekendTimeRanges(),
            temperatureControl.getHeatingControl().getWorkdayTimeRanges());

    Operation resultOperation = (requiredOperation == null) ? Operation.STANDBY : requiredOperation;
    int resultRequiredPower = 0;

    disableIfFunctionNotActive(temperatureControl, resultOperation);

    // Calculate operation
    if (!TimeRangeUtils.inTimeRange(floorHeatingTimeRanges)) {
      resultOperation = Operation.STANDBY;
      resultRequiredPower = 0;
    } else {
      if (deltaTemp < -floorHeatingTolerance) {
        resultOperation = Operation.FLOOR_HEATING;
        resultRequiredPower = 0;
      }
    }

    if (Operation.FLOOR_HEATING.equals(resultOperation)
        && (!floorHeatingEnabled || !TimeRangeUtils.inTimeRange(floorHeatingTimeRanges))) {
      resultOperation = Operation.STANDBY;
      resultRequiredPower = 0;
      } else
    if (deltaTemp < -forcedAirTolerance) {
      resultOperation = Operation.AIR_HEATING;
      resultRequiredPower = forcedAirControl.getForcedAirRequiredPower();
    }

    if (!forcedAirControl.isForcedAirEnabled()
        || !TimeRangeUtils.inTimeRange(forcedAirTimeRanges)
        || bme280ResponseDto.isError()) {
      requiredOperation = Operation.STANDBY;
      requiredPower = 0;
    }

    if (!Operation.AIR_CONDITION.equals(resultOperation) && deltaTemp > forcedAirTolerance) {
      resultOperation = Operation.AIR_COOLING;
      resultRequiredPower = forcedAirControl.getForcedAirRequiredPower();
    }

    if (deltaTemp > airConditionTolerance) {
      resultOperation = Operation.AIR_CONDITION;
      resultRequiredPower = forcedAirControl.getAirConditionRequiredPower();
    }

    if (!Operation.AIR_CONDITION.equals(resultOperation) && deltaTemp > forcedAirTolerance) {
      resultOperation = Operation.AIR_COOLING;
      resultRequiredPower = forcedAirControl.getForcedAirRequiredPower();
    }
    requiredOperation = resultOperation;
    requiredPower = !Operation.STANDBY.equals(resultOperation)?resultRequiredPower:0;
  }

  private void disableIfFunctionNotActive(final TemperatureControl temperatureControl, Operation resultOperation) {
    if (!temperatureControl.getHeatingControl().isHeatingEnabled()) {
      if (Operation.FLOOR_HEATING.equals(resultOperation)) {
        resultOperation = Operation.STANDBY;
      }
    }
    if (!temperatureControl.getForcedAirControl().isForcedAirEnabled()) {
      if ( List.of(Operation.AIR_HEATING, Operation.AIR_COOLING).contains(resultOperation)) {
        resultOperation = Operation.STANDBY;
      }
    }
    if (!temperatureControl.getForcedAirControl().isAirConditionEnabled()) {
      if (Operation.AIR_CONDITION.equals(resultOperation)) {
        resultOperation = Operation.STANDBY;
      }
    }
  }
}
