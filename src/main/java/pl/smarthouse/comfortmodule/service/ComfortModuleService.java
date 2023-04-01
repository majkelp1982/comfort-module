package pl.smarthouse.comfortmodule.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.configurations.ComfortModuleConfiguration;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleDao;
import pl.smarthouse.comfortmodule.model.dto.ComfortModuleDto;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ComfortModuleService {
  private final ComfortModuleConfiguration comfortModuleConfiguration;
  private final ModelMapper modelMapper = new ModelMapper();

  public Mono<ComfortModuleDao> getComfortModuleDao() {
    return Mono.just(comfortModuleConfiguration.getComfortModuleDao());
  }

  public final Mono<ComfortModuleDto> getComfortModule() {
    return Mono.just(
        modelMapper.map(comfortModuleConfiguration.getComfortModuleDao(), ComfortModuleDto.class));
  }
}
