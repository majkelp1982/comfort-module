package pl.smarthouse.comfortmodule.repository;

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
    // TODO implement cache to decrease number of mongoDB queries
    return reactiveMongoTemplate
        .findAll(ComfortModuleParamsDao.class, paramTableName)
        .last()
        .doOnSubscribe(subscription -> log.info("Getting params from table: {}", paramTableName));
  }
}
