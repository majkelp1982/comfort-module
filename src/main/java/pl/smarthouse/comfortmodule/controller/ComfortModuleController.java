package pl.smarthouse.comfortmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smarthouse.comfortmodule.service.ComfortModuleService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/comfort")
@RequiredArgsConstructor
public class ComfortModuleController {

  private final ComfortModuleService comfortModuleService;

  @GetMapping()
  public Mono<ComfortModuleDto> getControlModule() {
    return comfortModuleService.getComfortModule();
  }
}
