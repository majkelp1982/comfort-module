package pl.smarthouse.comfortmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.service.functions.HumidityService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.enums.Operation;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CalculateOperationService {
  private final ComfortModuleService comfortModuleService;
  private final HumidityService humidityService;

  public Mono<ComfortModuleDto> calculateOperation() {
    return comfortModuleService
        .getComfortModule()
        .flatMap(this::calculateOperation)
        .flatMap(comfortModuleService::updateAction);
  }

  private Mono<ComfortModuleDto> calculateOperation(final ComfortModuleDto comfortModuleDto) {
    comfortModuleDto.setLeftHoldTimeInMinutes(humidityService.getLeftHoldTimeInMinutes());

    if (Operation.HUMIDITY_ALERT.equals(humidityService.getRequiredOperation())) {
      comfortModuleDto.setAction(
          humidityService.getRequiredOperation(), humidityService.getRequiredPower());
    } else {
      comfortModuleDto.setStandby();
    }
    return Mono.just(comfortModuleDto);
  }
}
