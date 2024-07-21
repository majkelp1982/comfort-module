package pl.smarthouse.comfortmodule.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleParamsDao;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParamsRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<ComfortModuleParamsDao> saveParams(
      final ComfortModuleParamsDao comfortModuleParamsDao, final String paramTableName) {
    return reactiveMongoTemplate
        .remove(new Query(), ComfortModuleParamsDao.class, paramTableName)
        .then(reactiveMongoTemplate.save(comfortModuleParamsDao, paramTableName))
        .doOnSubscribe(subscription -> log.info("Saving params from table: {}", paramTableName));
  }

  public Mono<ComfortModuleParamsDao> getParams(final String paramTableName) {
    return reactiveMongoTemplate
        .findAll(ComfortModuleParamsDao.class, paramTableName)
        .collectList()
        .flatMap(this::getFirstConfiguration)
        .doOnSubscribe(subscription -> log.info("Getting params from table: {}", paramTableName))
        .doOnTerminate(() -> log.info("getParams finished"));
  }

  private Mono<ComfortModuleParamsDao> getFirstConfiguration(
      List<ComfortModuleParamsDao> comfortModuleParamsDaos) {
    if (comfortModuleParamsDaos.isEmpty()) {
      log.warn("Configuration has not been found");
      return Mono.empty();
    } else {
      log.info("Found {} configuration(s). Get first", comfortModuleParamsDaos.size());
      return Mono.just(comfortModuleParamsDaos.get(0));
    }
  }
}
