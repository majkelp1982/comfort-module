package pl.smarthouse.comfortmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import pl.smarthouse.sharedobjects.configuration.ModuleManagerConfiguration;
import pl.smarthouse.sharedobjects.dto.SettingsDto;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ModuleManagerService {
  private final ModuleManagerConfiguration moduleManagerConfiguration;

  public Mono<String> getServiceAddress(final String serviceName) {
    return getModuleSettingsByType(serviceName).map(settingsDto -> settingsDto.getServiceAddress());
  }

  private Mono<SettingsDto> getModuleSettingsByType(final String serviceName) {
    return Mono.just(moduleManagerConfiguration.moduleManagerWebClient())
        .flatMap(
            webClient ->
                webClient
                    .get()
                    .uri("/settings?type=" + serviceName)
                    .exchangeToMono(this::processResponse));
  }

  private Mono<SettingsDto> processResponse(final ClientResponse clientResponse) {
    if (HttpStatus.OK.equals(clientResponse.statusCode())) {
      return clientResponse.bodyToMono(SettingsDto.class);
    } else {
      return clientResponse.createException().flatMap(throwable -> Mono.error(throwable));
    }
  }
}
