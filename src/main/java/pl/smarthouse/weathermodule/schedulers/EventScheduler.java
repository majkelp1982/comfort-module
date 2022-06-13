package pl.smarthouse.weathermodule.schedulers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartmodule.model.actors.BME280.BME280;
import pl.smarthouse.smartmodule.model.actors.BME280.BME280Command;
import pl.smarthouse.smartmodule.model.actors.SDS011.SDS011;
import pl.smarthouse.smartmodule.model.actors.SDS011.SDS011Command;
import pl.smarthouse.smartmodule.services.ModuleService;
import pl.smarthouse.weathermodule.configurations.ModuleConfig;

@EnableScheduling
@AllArgsConstructor
@Service
@Slf4j
public class EventScheduler {
  ModuleService moduleService;
  ModuleConfig moduleConfig;

  @Scheduled(fixedDelay = 10000)
  public void eventScheduler() {
    moduleService.exchange();
    final BME280 bme280 = (BME280) moduleConfig.getActor(ModuleConfig.BME280);
    final SDS011 sds011 = (SDS011) moduleConfig.getActor(ModuleConfig.SDS011);
    bme280.setCommand(BME280Command.READ);
    sds011.setCommand(SDS011Command.SLEEP);
  }
}
