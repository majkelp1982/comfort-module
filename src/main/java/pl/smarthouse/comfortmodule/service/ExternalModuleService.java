package pl.smarthouse.comfortmodule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smarthouse.comfortmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ExternalModuleService {
  private final WebClient externalModuleWebClient;
  private final Esp32ModuleConfig esp32ModuleConfig;

  public Mono<String> sendBME280DataToExternalModule(final Bme280 bme280) {
    return externalModuleWebClient
        .post()
        .uri("/action")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(getCommand(bme280))
        .exchangeToMono(
            clientResponse -> {
              if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse.bodyToMono(String.class);
              } else {
                return clientResponse.createException().flatMap(Mono::error);
              }
            });
  }

  private String getCommand(final Bme280 bme280) {
    final ObjectMapper mapper = new ObjectMapper();
    final ObjectNode node = mapper.createObjectNode();
    node.put("zoneNumber", esp32ModuleConfig.getComfortZone().getZoneNumber());
    node.put("temperature", Math.round((bme280.getResponse().getTemperature()) * 10.0) / 10.0);
    node.put("humidity", bme280.getResponse().getHumidity());
    String result = null;
    try {
      result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    } catch (final JsonProcessingException e) {
      e.printStackTrace();
    }
    return result;
  }
}
