package pl.smarthouse.comfortmodule.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Esp32ModuleProperties {

  // Module specific
  public static final String FIRMWARE = "20241209.21";
  public static final String VERSION = "20241209.21";

  @Value("${module.zonename}")
  private String zoneName;

  @Value("${module.macaddress}")
  private String macAddress;
}
