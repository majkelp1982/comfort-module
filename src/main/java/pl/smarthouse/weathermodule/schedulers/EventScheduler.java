package pl.smarthouse.weathermodule.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.loghandler.service.LogService;
import pl.smarthouse.smartmodule.services.ModuleManagerService;
import pl.smarthouse.weathermodule.configurations.ModuleConfig;

import javax.annotation.PostConstruct;

@EnableScheduling
@RequiredArgsConstructor
@Service
@Slf4j
public class EventScheduler {

  ModuleManagerService moduleManagerService;
  @Autowired
  ModuleConfig moduleConfig;
  @Autowired
  LogService logService;

  @PostConstruct
  public void postConstruct() {
    moduleManagerService = new ModuleManagerService(moduleConfig.getConfiguration(), logService);
    }

  @Scheduled(fixedDelay = 10000)
  public void eventScheduler() {
    moduleManagerService
        .retrieveModuleIP()
        .doOnError(throwable -> log.error(throwable.getMessage()))
        .subscribe();
  }
}
