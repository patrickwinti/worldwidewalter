package ch.zhaw.www.utils;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Round;

/**
 * Interface to denote that the changes done to the object are of transactional nature, meaning the object will be
 * changed and then saved. This is a pessimistic lock behavior
 *
 * @param <R> optional return type if needed
 */
@FunctionalInterface
public interface RoundTransaction<R> {
    /**
     * Allows for change to given object before saving
     *
     * @param game  game that will be edited
     * @param round round that will be edited
     * @return optional value in case necessary
     */
    R transactionalChange(Game game, Round round);
}
