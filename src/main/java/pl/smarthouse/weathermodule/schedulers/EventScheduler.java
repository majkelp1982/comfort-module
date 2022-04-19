package pl.smarthouse.weathermodule.schedulers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartmodule.services.ModuleService;

@EnableScheduling
@AllArgsConstructor
@Service
@Slf4j
public class EventScheduler {
  ModuleService moduleService;

  @Scheduled(fixedDelay = 10000)
  public void eventScheduler() {
    moduleService.sendCommand();
  }
}
