package pl.smarthouse.comfortmodule.enums;

import lombok.Getter;
import pl.smarthouse.sharedobjects.enums.ZoneName;

@Getter
public class ComfortZone {
  private final String comfortZoneName;

  public ComfortZone(final ZoneName zoneName) {
    comfortZoneName = "COMFORT_" + zoneName.toString().toUpperCase();
  }
}
