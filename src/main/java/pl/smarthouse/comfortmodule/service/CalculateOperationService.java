package pl.smarthouse.comfortmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.service.functions.AirExchangerService;
import pl.smarthouse.comfortmodule.service.functions.ForcedAirAndHeatingService;
import pl.smarthouse.comfortmodule.service.functions.HumidityService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.enums.Operation;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CalculateOperationService {
  private final ComfortModuleService comfortModuleService;
  private final HumidityService humidityService;
  private final ForcedAirAndHeatingService forcedAirAndHeatingService;
  private final AirExchangerService airExchangerService;

  public Mono<ComfortModuleDto> calculateOperation() {
    return comfortModuleService
        .getComfortModule()
        .flatMap(this::calculateOperation)
        .flatMap(comfortModuleService::updateAction);
  }

  private Mono<ComfortModuleDto> calculateOperation(final ComfortModuleDto comfortModuleDto) {
    comfortModuleDto.setLeftHoldTimeInMinutes(humidityService.getLeftHoldTimeInMinutes());

    if (!Operation.STANDBY.equals(humidityService.getRequiredOperation())) {
      comfortModuleDto.setAction(
          humidityService.getRequiredOperation(), humidityService.getRequiredPower());
    } else if (!Operation.STANDBY.equals(forcedAirAndHeatingService.getRequiredOperation())) {
      comfortModuleDto.setAction(
          forcedAirAndHeatingService.getRequiredOperation(),
          forcedAirAndHeatingService.getRequiredPower());
    } else if (!Operation.STANDBY.equals(airExchangerService.getRequiredOperation())) {
      comfortModuleDto.setAction(
          airExchangerService.getRequiredOperation(), airExchangerService.getRequiredPower());
    } else {
      comfortModuleDto.setStandby();
    }
    return Mono.just(comfortModuleDto);
  }
}
