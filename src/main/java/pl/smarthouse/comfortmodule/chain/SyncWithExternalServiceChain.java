package pl.smarthouse.comfortmodule.chain;

import static pl.smarthouse.comfortmodule.properties.ActorProperties.BME280;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.comfortmodule.service.ExternalModuleService;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;

@Service
public class SyncWithExternalServiceChain {

  private final Bme280 bme280;
  private final ExternalModuleService externalModuleService;

  public SyncWithExternalServiceChain(
      @Autowired final ExternalModuleService externalModuleService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final ChainService chainService) {
    this.externalModuleService = externalModuleService;
    bme280 = (Bme280) esp32ModuleConfig.getConfiguration().getActorMap().getActor(BME280);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Sync BME280 read with external comfort service");
    // Wait 30 seconds and read send BME280 values
    chain.addStep(wait30secondsAndSendBme280Value());
    return chain;
  }

  private Step wait30secondsAndSendBme280Value() {

    return Step.builder()
        .stepDescription("Read value from sensor type BME280")
        .conditionDescription("Waiting 30 seconds")
        .condition(PredicateUtils.delaySeconds(30))
        .action(sendReadCommand())
        .build();
  }

  private Runnable sendReadCommand() {
    return () -> externalModuleService.sendBME280DataToExternalModule(bme280).subscribe();
  }
}
