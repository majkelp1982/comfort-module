package pl.smarthouse.comfortmodule.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.configurations.HeatingModuleConfiguration;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.heating.TempZoneDto;
import pl.smarthouse.sharedobjects.enums.Operation;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class HeatingModuleService {
  private final ComfortModuleService comfortModuleService;
  private final ComfortModuleParamsService comfortModuleParamsService;
  private final HeatingModuleConfiguration heatingModuleConfiguration;

  @Scheduled(initialDelay = 10000, fixedDelay = 30000)
  private void syncComfortModuleDataWithHeatingModule() {
    comfortModuleService
        .getComfortModule()
        .flatMap(
            comfortModuleDto -> {
              if (comfortModuleDto.getSensorResponse().isError()) {
                return Mono.empty();
              }
              return comfortModuleParamsService
                  .getParams()
                  .flatMap(
                      comfortModuleParamsDto ->
                          createTempZoneDto(comfortModuleDto, comfortModuleParamsDto));
            })
        .doOnNext(tempZoneDto -> log.info("Sending to heating module: {}", tempZoneDto))
        .flatMap(this::sendComfortModuleTemperature)
        .block();
  }

  private Mono<TempZoneDto> createTempZoneDto(
      final ComfortModuleDto comfortModuleDto,
      final ComfortModuleParamsDto comfortModuleParamsDto) {
    final TempZoneDto zoneDto = new TempZoneDto();
    zoneDto.setZoneNumber(getZoneNumber(comfortModuleDto.getType()));
    zoneDto.setTemperature((float) comfortModuleDto.getSensorResponse().getTemperature());

    if (List.of(Operation.AIR_HEATING, Operation.FLOOR_HEATING)
        .contains(comfortModuleDto.getCurrentOperation())) {
      zoneDto.setReqTemperature(
          (float) comfortModuleParamsDto.getTemperatureControl().getRequiredTemperature());
    } else {
      zoneDto.setReqTemperature(0);
    }

    if (zoneDto.getZoneNumber() != -1 && zoneDto.getZoneNumber() < 7) {
      return Mono.just(zoneDto);
    } else {
      return Mono.empty();
    }
  }

  private int getZoneNumber(final String moduleName) {
    switch (moduleName) {
      case "COMFORT_SALON":
        return 0;
      case "COMFORT_PRALNIA":
        return 1;
      case "COMFORT_LAZ_DOL":
        return 2;
      case "COMFORT_RODZICE":
        return 3;
      case "COMFORT_NATALIA":
        return 4;
      case "COMFORT_KAROLINA":
        return 5;
      case "COMFORT_LAZ_GORA":
        return 6;
      default:
        return -1;
    }
  }

  private Mono<String> sendComfortModuleTemperature(final TempZoneDto tempZoneDto) {

    return heatingModuleConfiguration
        .getWebClient()
        .post()
        .uri("/comfort")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(tempZoneDto)
        .exchangeToMono(
            clientResponse -> {
              if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse.bodyToMono(String.class);
              } else {
                return clientResponse.createException().flatMap(Mono::error);
              }
            });
  }
}
