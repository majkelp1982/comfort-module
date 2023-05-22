package pl.smarthouse.comfortmodule.model.dao;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Data
@SuperBuilder
public class ComfortModuleDao extends ModuleDao {
  private Bme280Response sensorResponse;
  private Operation currentOperation;
  private int requiredPower;
  private long leftHoldTimeInMinutes;
}
