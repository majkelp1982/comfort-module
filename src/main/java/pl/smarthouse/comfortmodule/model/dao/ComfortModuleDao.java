package pl.smarthouse.comfortmodule.model.dao;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Data
@SuperBuilder
public class ComfortModuleDao extends ModuleDao {
  private Bme280Response sensorResponse;
  @Transient private FunctionType functionType;
  private Operation currentOperation;
  private int requiredPower;
  private long leftHoldTimeInMinutes;
}
