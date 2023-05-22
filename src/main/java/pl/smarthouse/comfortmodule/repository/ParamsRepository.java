package pl.smarthouse.comfortmodule.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.smarthouse.comfortmodule.model.dao.ComfortModuleParamsDao;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ParamsRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<ComfortModuleParamsDao> saveParams(
      final ComfortModuleParamsDao comfortModuleParamsDao, final String paramTableName) {
    return reactiveMongoTemplate
        .remove(new Query(), ComfortModuleParamsDao.class)
        .then(reactiveMongoTemplate.save(comfortModuleParamsDao, paramTableName));
  }

  public Mono<ComfortModuleParamsDao> getParams(final String paramTableName) {
    return reactiveMongoTemplate.findAll(ComfortModuleParamsDao.class, paramTableName).last();
  }
}
