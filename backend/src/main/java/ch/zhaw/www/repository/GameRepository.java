package ch.zhaw.www.repository;

import ch.zhaw.www.model.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository with all the methods necessary to save, retrieve or delete games
 */
@Repository
public interface GameRepository extends CrudRepository<Game, String> {
}