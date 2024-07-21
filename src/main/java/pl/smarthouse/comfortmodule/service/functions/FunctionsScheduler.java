package pl.smarthouse.comfortmodule.service.functions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class FunctionsScheduler {
  private final AirExchangerService airExchangerService;
  private final ForcedAirAndHeatingService forcedAirAndHeatingService;
  private final HumidityService humidityService;

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  public void runSchedulers() {
    airExchangerService
        .scheduler()
        .then(forcedAirAndHeatingService.scheduler())
        .then(humidityService.scheduler())
        .doOnSubscribe(subscription -> log.info("Executing function schedulers"))
        .subscribe();
  }
}
