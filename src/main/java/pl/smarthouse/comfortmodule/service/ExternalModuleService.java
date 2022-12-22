package pl.smarthouse.comfortmodule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smarthouse.comfortmodule.configurations.ModuleConfig;
import pl.smarthouse.smartmodule.model.actors.typelibs.BME280.BME280;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ExternalModuleService {
  private final WebClient externalModuleWebClient;
  private final ModuleConfig moduleConfig;

  public Mono<String> sendBME280DataToExternalModule(final BME280 bme280) {
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

  private String getCommand(final BME280 bme280) {
    final ObjectMapper mapper = new ObjectMapper();
    final ObjectNode node = mapper.createObjectNode();
    node.put("zoneNumber", moduleConfig.getComfortZone().getZoneNumber());
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
