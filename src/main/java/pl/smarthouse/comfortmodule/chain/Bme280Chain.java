package pl.smarthouse.comfortmodule.chain;

import static pl.smarthouse.comfortmodule.properties.ActorProperties.BME280;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280CommandType;

@Service
public class Bme280Chain {

  private final Bme280 bme280;

  public Bme280Chain(
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final ChainService chainService) {
    bme280 = (Bme280) esp32ModuleConfig.getConfiguration().getActorMap().getActor(BME280);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Read BME280 sensor");
    // Wait 30 seconds and read values from sensor type BME280
    chain.addStep(wait30secondsAndReadBme280Value());
    // Wait until command read successful and set command to NO_ACTION for all
    chain.addStep(waitForResponseAndSetNoAction());
    return chain;
  }

  private Step wait30secondsAndReadBme280Value() {

    return Step.builder()
        .stepDescription("Read value from sensor type BME280")
        .conditionDescription("Waiting 30 seconds")
        .condition(PredicateUtils.delaySeconds(30))
        .action(sendReadCommand())
        .build();
  }

  private Runnable sendReadCommand() {
    return () -> bme280.getCommandSet().setCommandType(Bme280CommandType.READ);
  }

  private Step waitForResponseAndSetNoAction() {
    return Step.builder()
        .stepDescription("Set BME280 command to NO_ACTION")
        .conditionDescription("Wait until command read successful")
        .condition(PredicateUtils.isResponseUpdated(bme280))
        .action(setNoAction())
        .build();
  }

  private Runnable setNoAction() {
    return () -> bme280.getCommandSet().setCommandType(Bme280CommandType.NO_ACTION);
  }
}
