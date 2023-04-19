package pl.smarthouse.comfortmodule.model.dao;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Data
@SuperBuilder
public class ComfortModuleDao extends ModuleDao {
  private Bme280Response sensorResponse;
  @Transient private boolean heatingEnabled;
}
