package pl.smarthouse.comfortmodule.service.functions;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.service.ComfortModuleParamsService;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.comfortmodule.utils.TimeRangeUtils;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.comfort.core.AirExchanger;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRangeMode;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;
import pl.smarthouse.sharedobjects.enums.Operation;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Getter
public class AirExchangerService {
  private final ComfortModuleService comfortModuleService;
  private final ComfortModuleParamsService comfortModuleParamsService;
  private final LocalDateTime humidityOverLimitTimestamp = LocalDateTime.now();
  private Bme280ResponseDto bme280ResponseDto;
  private Operation requiredOperation = Operation.STANDBY;
  private int requiredPower = 0;

  public Mono<AirExchanger> scheduler() {
    return comfortModuleService
        .getBme280SensorDto()
        .doOnNext(sensor -> bme280ResponseDto = sensor)
        .map(ignore -> comfortModuleParamsService.getParams())
        .map(ComfortModuleParamsDto::getAirExchanger)
        .doOnNext(this::calculateOperation);
  }

  private void calculateOperation(final AirExchanger airExchanger) {
    final Set<TimeRange> timeRanges =
        TimeRangeUtils.getTimeRangesByDayOfTheWeek(
            TimeRangeMode.AUTO,
            airExchanger.getWeekendTimeRanges(),
            airExchanger.getWorkdayTimeRanges());

    // Calculate operation
    if (!airExchanger.isEnabled() || !TimeRangeUtils.inTimeRange(timeRanges)) {
      requiredOperation = Operation.STANDBY;
      requiredPower = 0;
    } else {
      requiredOperation = Operation.AIR_EXCHANGE;
      requiredPower = airExchanger.getRequiredPower();
    }
  }
}
