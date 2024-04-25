package pl.smarthouse.comfortmodule.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.configurations.ComfortModuleConfiguration;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ComfortModuleService {
  private final ComfortModuleConfiguration comfortModuleConfiguration;
  private final ModelMapper modelMapper = new ModelMapper();

  public final Mono<ComfortModuleDto> getComfortModule() {
    return Mono.just(
        modelMapper.map(comfortModuleConfiguration.getComfortModuleDao(), ComfortModuleDto.class));
  }

  public final Mono<Bme280ResponseDto> getBme280SensorDto() {
    return Mono.just(
            modelMapper.map(
                comfortModuleConfiguration.getComfortModuleDao(), ComfortModuleDto.class))
        .map(ComfortModuleDto::getSensorResponse);
  }

  public final Bme280Response getBme280SensorResponse() {
    return comfortModuleConfiguration.getComfortModuleDao().getSensorResponse();
  }

  public final Mono<Bme280Response> setBme280Sensor(final Bme280Response bme280Response) {
    return Mono.just(comfortModuleConfiguration.getComfortModuleDao())
        .doOnNext(comfortModuleDao -> comfortModuleDao.setSensorResponse(bme280Response))
        .thenReturn(bme280Response);
  }

  public final Mono<ComfortModuleDto> updateAction(final ComfortModuleDto comfortModuleDto) {
    return Mono.just(comfortModuleConfiguration.getComfortModuleDao())
        .doOnNext(
            comfortModuleDao -> {
              comfortModuleDao.setCurrentOperation(comfortModuleDto.getCurrentOperation());
              comfortModuleDao.setRequiredPower(comfortModuleDto.getRequiredPower());
              comfortModuleDao.setLeftHoldTimeInMinutes(
                  comfortModuleDto.getLeftHoldTimeInMinutes());
            })
        .thenReturn(comfortModuleDto);
  }

  public Mono<String> getType() {
    return Mono.just(comfortModuleConfiguration.getComfortModuleDao().getType().toLowerCase());
  }

  public void setEnableTemperatureTimeRanges(boolean enabled) {
    comfortModuleConfiguration
        .getComfortModuleDao()
        .setEnableTemperatureTimeRanges(enabled ? State.ON : State.OFF);
  }

  public State getEnableTemperatureTimeRanges() {
    return comfortModuleConfiguration.getComfortModuleDao().getEnableTemperatureTimeRanges();
  }
}
