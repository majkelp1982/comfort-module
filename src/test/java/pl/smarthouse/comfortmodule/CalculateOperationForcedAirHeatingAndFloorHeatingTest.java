package pl.smarthouse.comfortmodule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.smarthouse.comfortmodule.service.ComfortModuleParamsService;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.comfortmodule.service.functions.ForcedAirAndHeatingService;
import pl.smarthouse.sharedobjects.dto.comfort.core.ForcedAirControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.HeatingControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.TemperatureControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRange;
import pl.smarthouse.sharedobjects.enums.Operation;

public class CalculateOperationForcedAirHeatingAndFloorHeatingTest {
  @MockBean ComfortModuleService comfortModuleService;
  @MockBean ComfortModuleParamsService comfortModuleParamsService;
  ForcedAirAndHeatingService forcedAirAndHeatingService =
      new ForcedAirAndHeatingService(comfortModuleService, comfortModuleParamsService);

  @Test
  void test1() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: sensor has error. No temp read
    // when: calculating operation
    // then: STANDBY and power 0

    setCurrentTemperature(0.0);
    setBmeError(true);
    setStandbyOperation();

    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());

    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test2() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: sensor ok. temperature in range
    // when: calculating operation
    // then: STANDBY and power 0

    setStandbyOperation();
    setCurrentTemperature(24.2);
    setBmeError(false);

    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());

    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test3() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: temperature to low
    // when: calculating operation
    // then: FLOOR_HEATING and power 0

    setStandbyOperation();
    setCurrentTemperature(24.0);
    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());
    assertResults(Operation.STANDBY, 0);

    setCurrentTemperature(23.9);
    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());
    assertResults(Operation.FLOOR_HEATING, 0);
  }

  @Test
  void test4() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: FLOOR_HEATING, temperature to low but heating switch to not enabled
    // when: calculating operation
    // then: STANDBY and power 0

    setCurrentOperation(Operation.FLOOR_HEATING, 0);
    setCurrentTemperature(23.9);
    final TemperatureControl temperatureControl = createTemperatureControl();
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.FLOOR_HEATING, 0);

    temperatureControl.getHeatingControl().setHeatingEnabled(false);

    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test5() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: STANDBY, temperature to low heating not enabled
    // when: calculating operation
    // then: stay STANDBY and power 0

    setStandbyOperation();
    setCurrentTemperature(23.9);
    final TemperatureControl temperatureControl = createTemperatureControl();
    temperatureControl.getHeatingControl().setHeatingEnabled(false);

    forcedAirAndHeatingService.calculateOperation(temperatureControl);

    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test6() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: temperature low, FLOOR_HEATING, and then even lower temperature
    // when: calculating operation
    // then: AIR_HEATING and power 40

    final TemperatureControl temperatureControl = createTemperatureControl();
    setStandbyOperation();
    setCurrentTemperature(23.8);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.FLOOR_HEATING, 0);

    setCurrentTemperature(23.7);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_HEATING, 40);
  }

  @Test
  void test7() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: AIR_HEATING, temperature low, but higher than tolerance
    // when: calculating operation
    // then: stay AIR_HEATING and power 40 until air is delta+(air-0.5)

    final TemperatureControl temperatureControl = createTemperatureControl();
    setCurrentOperation(Operation.AIR_HEATING, 40);

    setCurrentTemperature(24.3);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_HEATING, 40);
  }

  @Test
  void test8() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: AIR_HEATING, temperature low, but higher than tolerance
    // when: calculating operation
    // then: switch from AIR_HEATING and power 40 to FLOOR_HEATING

    final TemperatureControl temperatureControl = createTemperatureControl();
    setCurrentOperation(Operation.AIR_HEATING, 40);

    setCurrentTemperature(24.2);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_HEATING, 40);

    setCurrentTemperature(24.4);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.FLOOR_HEATING, 0);
  }

  @Test
  void test9() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: FLOOR_HEATING, temperature reach required temp
    // when: calculating operation
    // then: STANDBY and power 0
    final TemperatureControl temperatureControl = createTemperatureControl();
    setCurrentOperation(Operation.FLOOR_HEATING, 0);

    setCurrentTemperature(24.4);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.FLOOR_HEATING, 0);
    setCurrentTemperature(24.5);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.STANDBY, 0);
  }

  private void setStandbyOperation() {
    forcedAirAndHeatingService.setRequiredOperation(Operation.STANDBY);
  }

  private void setCurrentTemperature(final double temperature) {
    forcedAirAndHeatingService.getBme280ResponseDto().setTemperature(temperature);
  }

  private void setCurrentOperation(final Operation operation, final int power) {
    forcedAirAndHeatingService.setRequiredOperation(operation);
    forcedAirAndHeatingService.setRequiredPower(power);
  }

  private void setBmeError(final boolean error) {
    forcedAirAndHeatingService.getBme280ResponseDto().setError(error);
  }

  private TemperatureControl createTemperatureControl() {
    final HeatingControl heatingControl = mockHeatingControl(true, 0.6, 0.5);
    final ForcedAirControl forcedAirControl = mockForcedAirControl(true, true, 0.7, 1.1, 40, 75);
    final TemperatureControl temperatureControl =
        mockTemperatureControl(24.5, heatingControl, forcedAirControl);
    return temperatureControl;
  }

  private TemperatureControl mockTemperatureControl(
      final double requiredTemperature,
      final HeatingControl heatingControl,
      final ForcedAirControl forcedAirControl) {
    return TemperatureControl.builder()
        .requiredTemperature(requiredTemperature)
        .heatingControl(heatingControl)
        .forcedAirControl(forcedAirControl)
        .build();
  }

  private ForcedAirControl mockForcedAirControl(
      final boolean forcedAirEnabled,
      final boolean airConditionEnabled,
      final double forcedAirTolerance,
      final double airConditionTolerance,
      final int forcedAirRequiredPower,
      final int airConditionRequiredPower) {
    return ForcedAirControl.builder()
        .forcedAirEnabled(forcedAirEnabled)
        .airConditionEnabled(airConditionEnabled)
        .forcedAirTolerance(forcedAirTolerance)
        .airConditionTolerance(airConditionTolerance)
        .forcedAirRequiredPower(forcedAirRequiredPower)
        .airConditionRequiredPower(airConditionRequiredPower)
        .workdayTimeRanges(fullDayTimeRange())
        .weekendTimeRanges(fullDayTimeRange())
        .build();
  }

  private HeatingControl mockHeatingControl(
      final boolean enabled, final double overheatingOn2Tariff, final double lowTolerance) {
    return HeatingControl.builder()
        .heatingEnabled(enabled)
        .overheatingOn2Tariff(overheatingOn2Tariff)
        .lowTolerance(lowTolerance)
        .workdayTimeRanges(fullDayTimeRange())
        .weekendTimeRanges(fullDayTimeRange())
        .build();
  }

  private Set<TimeRange> fullDayTimeRange() {
    return Set.of(TimeRange.builder().from(LocalTime.MIN).to(LocalTime.MAX).build());
  }

  private void assertResults(final Operation operation, final int requiredPower) {
    assertEquals(operation, forcedAirAndHeatingService.getRequiredOperation());
    assertEquals(requiredPower, forcedAirAndHeatingService.getRequiredPower());
  }
}
