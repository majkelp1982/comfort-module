package pl.smarthouse.comfortmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.comfortmodule.service.ComfortModuleParamsService;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ComfortModuleController {

  private final ComfortModuleService comfortModuleService;
  private final ComfortModuleParamsService comfortModuleParamsService;

  @GetMapping("/comfort")
  public Mono<ComfortModuleDto> getControlModule() {
    return comfortModuleService.getComfortModule();
  }

  @PostMapping("/params")
  public Mono<ComfortModuleParamsDto> saveParams(
      @RequestBody final ComfortModuleParamsDto comfortModuleSettingsDto) {
    return comfortModuleParamsService.saveParams(comfortModuleSettingsDto);
  }

  @GetMapping("/params")
  public Mono<ComfortModuleParamsDto> getParams() {
    return Mono.just(comfortModuleParamsService.getParams());
  }
}
