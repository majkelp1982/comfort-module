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
import pl.smarthouse.sharedobjects.dto.core.TimeRange;
import pl.smarthouse.sharedobjects.enums.Operation;

public class CalculateOperationForcedAirCoolingAndAirConditionTest {
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
    setCurrentTemperature(25.2);
    setBmeError(false);

    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());

    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test3() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: temperature to high
    // when: calculating operation
    // then: AIR_COOLING and power 40

    setStandbyOperation();
    setCurrentTemperature(25.2);
    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());
    assertResults(Operation.STANDBY, 0);

    setCurrentTemperature(25.3);
    forcedAirAndHeatingService.calculateOperation(createTemperatureControl());
    assertResults(Operation.AIR_COOLING, 40);
  }

  @Test
  void test4() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: AIR_COOLING, temperature to high but air cooling switch to not enabled
    // when: calculating operation
    // then: STANDBY and power 0

    setCurrentOperation(Operation.AIR_COOLING, 40);
    setCurrentTemperature(25.3);
    final TemperatureControl temperatureControl = createTemperatureControl();
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_COOLING, 40);

    temperatureControl.getForcedAirControl().setForcedAirEnabled(false);

    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test5() {
    // params: Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40, airConPower:
    // 75, time ranges: whole day
    // given: STANDBY, temperature to high but forced air not enabled
    // when: calculating operation
    // then: stay STANDBY and power 0

    setStandbyOperation();
    setCurrentTemperature(25.3);
    final TemperatureControl temperatureControl = createTemperatureControl();
    temperatureControl.getForcedAirControl().setForcedAirEnabled(false);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);

    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test6() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: temperature high, AIR_COOLING, and then even higher temperature
    // when: calculating operation
    // then: AIR_CONDITION and power 75

    final TemperatureControl temperatureControl = createTemperatureControl();
    setStandbyOperation();
    setCurrentTemperature(25.6);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_COOLING, 40);

    setCurrentTemperature(25.7);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_CONDITION, 75);
  }

  @Test
  void test7() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: temperature high, AIR_COOLING, and then even higher temperature, but AIR_CON not
    // enabled
    // when: calculating operation
    // then: AIR_COOLING and power 40

    final TemperatureControl temperatureControl = createTemperatureControl();
    setStandbyOperation();
    setCurrentTemperature(25.6);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_COOLING, 40);

    setCurrentTemperature(25.7);
    temperatureControl.getForcedAirControl().setAirConditionEnabled(false);

    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_COOLING, 40);
  }

  @Test
  void test09() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: AIR_COOLING, temperature reach required temp
    // when: calculating operation
    // then: STANDBY and power 0
    final TemperatureControl temperatureControl = createTemperatureControl();
    setCurrentOperation(Operation.AIR_COOLING, 40);

    setCurrentTemperature(24.6);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_COOLING, 40);
    setCurrentTemperature(24.5);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.STANDBY, 0);
  }

  @Test
  void test10() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: AIR_CONDITION, temperature below -0.1 airConTolerance
    // when: calculating operation
    // then: AIR_CONDITION and power 75
    final TemperatureControl temperatureControl = createTemperatureControl();
    setCurrentOperation(Operation.AIR_CONDITION, 75);

    setCurrentTemperature(25.6);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_CONDITION, 75);
    setCurrentTemperature(25.5);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_CONDITION, 75);
  }

  @Test
  void test11() {
    // description. Required temp: 24.5, heating: 0.5, air: 0.7, airCon: 1.1, airPower:40,
    // airConPower: 75, time ranges: whole day
    // given: AIR_CONDITION, temperature below -0.2 airConTolerance
    // when: calculating operation
    // then: AIR_COOLING and power 40
    final TemperatureControl temperatureControl = createTemperatureControl();
    setCurrentOperation(Operation.AIR_CONDITION, 75);

    setCurrentTemperature(25.5);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_CONDITION, 75);
    setCurrentTemperature(25.4);
    forcedAirAndHeatingService.calculateOperation(temperatureControl);
    assertResults(Operation.AIR_COOLING, 40);
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
    final HeatingControl heatingControl = mockHeatingControl(false, 0.6, 0.5);
    final ForcedAirControl forcedAirControl = mockForcedAirControl(true, true, 0.7, 1.1, 40, 75);
    return mockTemperatureControl(24.5, heatingControl, forcedAirControl);
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
