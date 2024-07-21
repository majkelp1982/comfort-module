package pl.smarthouse.comfortmodule.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleParamsDao;
import pl.smarthouse.comfortmodule.repository.ParamsRepository;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComfortModuleParamsService {
  private final ParamsRepository paramsRepository;
  private final ComfortModuleService comfortModuleService;
  private final ModelMapper modelMapper = new ModelMapper();
  private ComfortModuleParamsDto comfortModuleParamsDto;

  public Mono<ComfortModuleParamsDto> saveParams(
      final ComfortModuleParamsDto comfortModuleParamsDto) {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository.saveParams(
                    modelMapper.map(comfortModuleParamsDto, ComfortModuleParamsDao.class),
                    paramTableName))
        .doOnNext(comfortModuleParamsDao -> refreshParams())
        .map(
            comfortModuleParamsDao ->
                modelMapper.map(comfortModuleParamsDao, ComfortModuleParamsDto.class));
  }

  private Mono<String> getParamTableName() {
    return comfortModuleService.getModuleName().map(type -> type + "_settings");
  }

  public ComfortModuleParamsDto getParams() {
    if (comfortModuleParamsDto == null) {
      log.info("Configuration not ready");
      refreshParams();
    }

    return comfortModuleParamsDto;
  }

  @Scheduled(initialDelay = 5000, fixedDelay = 60 * 1000)
  private void refreshParams() {
    getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository
                    .getParams(paramTableName)
                    .doOnNext(
                        comfortModuleParamsDao ->
                            log.debug("Successfully retrieve params: {}", comfortModuleParamsDao))
                    .map(
                        comfortModuleParamsDao ->
                            comfortModuleParamsDto =
                                modelMapper.map(
                                    comfortModuleParamsDao, ComfortModuleParamsDto.class))
                    .onErrorResume(
                        NoSuchElementException.class,
                        throwable -> {
                          log.warn("No params found for: {}", paramTableName);
                          return Mono.empty();
                        })
                    .doOnError(
                        throwable ->
                            log.error(
                                "Error on get params. Error message: {}",
                                throwable.getMessage(),
                                throwable))
                    .doOnSubscribe(
                        subscription ->
                            log.debug("Get module params from collection: {}", paramTableName)))
        .subscribe();
  }
}
