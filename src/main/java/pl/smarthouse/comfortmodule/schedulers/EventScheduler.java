package pl.smarthouse.comfortmodule.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.configurations.ModuleConfig;
import pl.smarthouse.comfortmodule.service.ExternalModuleService;
import pl.smarthouse.smartmodule.model.actors.typelibs.BME280.BME280;
import pl.smarthouse.smartmodule.model.actors.typelibs.BME280.BME280CommandSet;
import pl.smarthouse.smartmodule.model.actors.typelibs.BME280.BME280CommandType;
import pl.smarthouse.smartmodule.services.ModuleService;

import java.util.Objects;

@EnableScheduling
@RequiredArgsConstructor
@Service
@Slf4j
public class EventScheduler {
  private final ModuleService moduleService;
  private final ModuleConfig moduleConfig;
  private final ExternalModuleService externalModuleService;

  @Scheduled(fixedDelay = 5000)
  public void eventScheduler() {
    moduleService.exchange();
    final BME280 bme280 = (BME280) moduleConfig.getActor(ModuleConfig.BME280);
    if (Objects.nonNull(bme280.getResponse())) {
      externalModuleService.sendBME280DataToExternalModule(bme280).subscribe();
    }
    bme280.setCommandSet(new BME280CommandSet(BME280CommandType.READ));
  }
}
