package org.aayush.cache.lock;

/**
 * @author Aayush Srivastava
 */
@FunctionalInterface
public interface LockHandler<T> {

    T handle() throws Throwable;
}
