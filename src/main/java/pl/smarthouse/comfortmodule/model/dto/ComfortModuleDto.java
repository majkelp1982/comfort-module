package pl.smarthouse.comfortmodule.model.dto;

import lombok.Data;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Data
public class ComfortModuleDto {
  private Bme280Response sensor;
}
