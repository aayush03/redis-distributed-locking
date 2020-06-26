package org.aayush.cache;


import org.aayush.cache.lock.LockHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author Aayush Srivastava
 */
public interface CacheTemplate {

    void putValue(final String key, final Object value, final String cacheName);

    Object getValue(String key, String cacheName);

    Object getValue(final String key);

    void clearAllCache(final String cacheName);

    void deleteValue(final String key);

    void deleteValue(String key, String cacheName);

    Boolean tryLock(String key, String value, long timeout, TimeUnit unit);

    <T> T tryLock(String key, LockHandler<T> handler, long timeout, int retries, long waitingTime) throws Throwable;

    <T> T tryLock(String key, LockHandler<T> handler, long timeout, int retries, long waitingTime, Class<? extends RuntimeException> onFailure) throws Throwable;

}
