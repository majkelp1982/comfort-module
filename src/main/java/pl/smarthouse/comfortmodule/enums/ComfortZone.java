package pl.smarthouse.comfortmodule.enums;

import lombok.Getter;
import pl.smarthouse.sharedobjects.enums.ZoneName;

@Getter
public class ComfortZone {
  private final int zoneNumber;
  private final String comfortZoneName;

  public ComfortZone(final ZoneName zoneName) {
    comfortZoneName = "COMFORT_" + zoneName.toString().toUpperCase();
    switch (comfortZoneName) {
      case "COMFORT_SALON":
        zoneNumber = 0;
        break;
      case "COMFORT_LAZ_DOL":
        zoneNumber = 1;
        break;
      case "COMFORT_PRALNIA":
        zoneNumber = 2;
        break;
      case "COMFORT_RODZICE":
        zoneNumber = 3;
        break;
      case "COMFORT_NATALIA":
        zoneNumber = 4;
        break;
      case "COMFORT_KAROLINA":
        zoneNumber = 5;
        break;
      case "COMFORT_LAZ_GORA":
        zoneNumber = 6;
        break;
      default:
        zoneNumber = 0;
    }
  }
}
