package pl.smarthouse.comfortmodule.service;

import java.time.Duration;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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

  public Mono<ComfortModuleParamsDto> saveParams(
      final ComfortModuleParamsDto comfortModuleParamsDto) {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository.saveParams(
                    modelMapper.map(comfortModuleParamsDto, ComfortModuleParamsDao.class),
                    paramTableName))
        .map(
            comfortModuleParamsDao ->
                modelMapper.map(comfortModuleParamsDao, ComfortModuleParamsDto.class));
  }

  private Mono<String> getParamTableName() {
    return comfortModuleService.getModuleName().map(moduleName -> moduleName + "_settings");
  }

  public Mono<ComfortModuleParamsDto> getParams() {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository
                    .getParams(paramTableName)
                    .doOnNext(
                        comfortModuleParamsDao ->
                            log.info("Successfully retrieve params: {}", comfortModuleParamsDao))
                    .map(
                        comfortModuleParamsDao ->
                            modelMapper.map(comfortModuleParamsDao, ComfortModuleParamsDto.class))
                    .onErrorResume(
                        NoSuchElementException.class,
                        throwable -> {
                          log.warn("No params found for: {}", paramTableName);
                          return Mono.empty();
                        })
                    .doOnError(
                        throwable ->
                            log.error(
                                "Error on get params. Error message: {}, Error: {}",
                                throwable.getMessage(),
                                throwable))
                    .doOnSubscribe(
                        subscription ->
                            log.info("Get module params from collection: {}", paramTableName)))
        .cache(Duration.ofMinutes(1));
  }
}
