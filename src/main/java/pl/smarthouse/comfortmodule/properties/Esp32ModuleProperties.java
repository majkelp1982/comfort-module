package pl.smarthouse.comfortmodule.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Esp32ModuleProperties {

  // Module specific
  public static final String FIRMWARE = "20231202.08";
  public static final String VERSION = "20231202.09";

  @Value("${module.zonename}")
  private String zoneName;

  @Value("${module.macaddress}")
  private String macAddress;
}
