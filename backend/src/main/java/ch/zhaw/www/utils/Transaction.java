package ch.zhaw.www.utils;

/**
 * Interface to denote that the changes done to the object are of transactional nature, meaning the object will be
 * changed and then saved. This is a pessimistic lock behavior
 *
 * @param <T> The editable object
 * @param <R> optional return type if needed
 */
@FunctionalInterface
public interface Transaction<T, R> {
    /**
     * Allows for change to given object before saving
     *
     * @param objectToChange object that will be edited
     * @return optional value in case necessary
     */
    R transactionalChange(T objectToChange);
}
