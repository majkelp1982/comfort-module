package pl.smarthouse.comfortmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import pl.smarthouse.comfortmodule.configurations.VentilationModuleConfiguration;
import pl.smarthouse.comfortmodule.exceptions.VentilationModuleServiceResponseException;
import pl.smarthouse.sharedobjects.dto.ventilation.ZoneDto;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import reactor.core.publisher.Mono;

@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class VentilationModuleService {
  private static final String VENTILATION_MODULE_TYPE = "VENTILATION";
  private final ComfortModuleService comfortModuleService;
  private final CalculateOperationService calculateOperationService;
  private final VentilationModuleConfiguration ventilationModuleConfiguration;
  private final ModuleManagerService moduleManagerService;

  private final String SERVICE_ADDRESS_REGEX =
      "^(?:http:\\/\\/)?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$";

  @Scheduled(fixedDelay = 5000)
  private void sendOperationToVentModule() {
    calculateOperationService
        .calculateOperation()
        .flatMap(
            comfortModuleDto ->
                sendCommand(
                    comfortModuleDto.getCurrentOperation(), comfortModuleDto.getRequiredPower()))
        .block();
  }

  public Mono<ZoneDto> sendCommand(final Operation operation, final int requestPower) {
    return comfortModuleService
        .getModuleName()
        .flatMap(
            type ->
                sendCommandToVentilationModule(
                    ZoneName.valueOf(prepareZoneName(type)), operation, requestPower));
  }

  private String prepareZoneName(final String type) {
    if (type.toUpperCase().contains("COMFORT")) {
      return type.substring(8).toUpperCase();
    } else {
      return type.toUpperCase();
    }
  }

  private Mono<String> retrieveVentilationServiceBaseUrl() {
    return Mono.justOrEmpty(ventilationModuleConfiguration.getBaseUrl())
        .switchIfEmpty(
            Mono.defer(() -> moduleManagerService.getServiceAddress(VENTILATION_MODULE_TYPE)))
        .flatMap(
            baseUrl -> {
              if (!baseUrl.matches(SERVICE_ADDRESS_REGEX)) {
                Mono.error(
                    new IllegalArgumentException(
                        String.format(
                            "Base url have to contain http address. Current: %s", baseUrl)));
              }
              ventilationModuleConfiguration.setBaseUrl(baseUrl);
              return Mono.just(baseUrl);
            });
  }

  private Mono<ZoneDto> sendCommandToVentilationModule(
      final ZoneName zoneName, final Operation operation, final int requestPower) {
    return retrieveVentilationServiceBaseUrl()
        .flatMap(signal -> Mono.just(ventilationModuleConfiguration.getWebClient()))
        .flatMap(
            webClient ->
                webClient
                    .post()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path("/zones/" + zoneName + "/operation")
                                .queryParam("operation", operation)
                                .queryParam("requestPower", requestPower)
                                .build())
                    .exchangeToMono(this::processResponse))
        .doOnError(
            throwable -> {
              ventilationModuleConfiguration.resetBaseUrl();
              log.error(
                  "Error occurred on sendCommandToVentilationModule. Reason: {}",
                  throwable.getMessage(),
                  throwable);
            });
  }

  private Mono<ZoneDto> processResponse(final ClientResponse clientResponse) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return clientResponse.bodyToMono(ZoneDto.class);
    } else {
      return clientResponse
          .bodyToMono(String.class)
          .flatMap(
              response ->
                  Mono.error(
                      new VentilationModuleServiceResponseException(
                          clientResponse.statusCode(), response)));
    }
  }
}
