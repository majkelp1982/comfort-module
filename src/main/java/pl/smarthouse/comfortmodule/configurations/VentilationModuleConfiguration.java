package pl.smarthouse.comfortmodule.configurations;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@Data
public class VentilationModuleConfiguration {
  private String baseUrl = null;

  public WebClient getWebClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient()))
        .build();
  }

  public void resetBaseUrl() {
    baseUrl = null;
  }

  private HttpClient httpClient() {
    return HttpClient.create()
        .baseUrl(baseUrl)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .responseTimeout(Duration.ofMillis(5000))
        .doOnConnected(
            conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10000, TimeUnit.MILLISECONDS)));
  }
}
