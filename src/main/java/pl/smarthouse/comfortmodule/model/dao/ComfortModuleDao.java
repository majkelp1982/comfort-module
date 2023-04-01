package pl.smarthouse.comfortmodule.model.dao;

import lombok.Builder;
import lombok.Data;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Data
@Builder
public class ComfortModuleDao {
  private Bme280Response sensorResponse;
}
