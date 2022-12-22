package pl.smarthouse.comfortmodule.enums;

public enum ComfortZone {
  COMFORT_SALON(0),
  COMFORT_LAZ_DOL(1),
  COMFORT_PRALNIA(2),
  COMFORT_RODZICE(3),
  COMFORT_NATALIA(4),
  COMFORT_KAROLINA(5),
  COMFORT_LAZ_GORA(6);

  private final int zone;

  ComfortZone(final int zone) {
    this.zone = zone;
  }

  public int getZoneNumber() {
    return zone;
  }
}
