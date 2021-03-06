import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Adds support for making methods returning T idempotent.
 * With the default values, the fact that an action has occurred is expired after 10 minutes.
 * This means it forgets the action was executed after 10 minutes.
 *
 * @param < T > the return value of the action
 */
trait Idempotent<T> {

    int maxSize = 1000
    long expiresAfterWriteDuration = 10
    TimeUnit expiresAfterWriteTimeUnit = TimeUnit.MINUTES


    private Cache<Object, T> previousActions = CacheBuilder.newBuilder().maximumSize(maxSize).expireAfterWrite(expiresAfterWriteDuration, expiresAfterWriteTimeUnit).build()

    /**
     * Execute some action idempotent. This will need a key that identifies the operation that should run idempotent.
     * When 2 calls are made with the same key, only the first one will run the action. The next call will be ignored.
     * When an exception is thrown by the action, the action will be executed again next time the call is made with the same key.
     *
     * @param key key to identify the action that should run idempotent.
     * @param action action returning T
     */
    T idempotent(Object key, Closure<T> action) {
        assert key, "Expected a key"
        return previousActions.get(key, action as Callable) as T

    }


}










