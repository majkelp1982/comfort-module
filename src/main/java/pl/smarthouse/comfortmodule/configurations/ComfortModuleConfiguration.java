package pl.smarthouse.comfortmodule.configurations;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleDao;
import pl.smarthouse.comfortmodule.properties.Esp32ModuleProperties;
import pl.smarthouse.comfortmodule.repository.ParamsRepository;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRangeMode;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.sharedobjects.utils.FunctionTypeUtil;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.EnumCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.properties.defaults.Bme280DefaultProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;
import pl.smarthouse.smartmonitoring.service.MonitoringService;

@Configuration
@RequiredArgsConstructor
@Getter
@Slf4j
public class ComfortModuleConfiguration {
  private final CompareProcessor compareProcessor;
  private final MonitoringService monitoringService;
  private final Esp32ModuleConfig esp32ModuleConfig;
  private final Esp32ModuleProperties esp32ModuleProperties;
  private final ParamsRepository paramsRepository;
  private ComfortModuleDao comfortModuleDao;

  @PostConstruct
  void postConstruct() {
    final Bme280Response sensor = new Bme280Response();
    sensor.setError(true);
    comfortModuleDao =
        ComfortModuleDao.builder()
            .type(esp32ModuleConfig.getComfortZone().getComfortZoneName())
            .functionType(
                FunctionTypeUtil.determinateFunctionType(
                    ZoneName.valueOf(esp32ModuleProperties.getZoneName())))
            .sensorResponse(sensor)
            .currentOperation(Operation.STANDBY)
            .timeRangeMode(TimeRangeMode.AUTO)
            .build();
    monitoringService.setModuleDaoObject(comfortModuleDao);
    setCompareProperties();
  }

  private void setCompareProperties() {
    compareProcessor.addMap("error", BooleanCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "errorPendingAcknowledge", BooleanCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "functionType", EnumCompareProperties.builder().saveEnabled(false).build());
    Bme280DefaultProperties.setDefaultProperties(compareProcessor, "sensorResponse");
    compareProcessor.addMap(
        "currentOperation", EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "requiredPower",
        NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build());
    compareProcessor.addMap(
        "leftHoldTimeInMinutes",
        NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build());
    compareProcessor.addMap(
        "enableTemperatureTimeRanges", EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "timeRangeMode", EnumCompareProperties.builder().saveEnabled(true).build());
  }
}
