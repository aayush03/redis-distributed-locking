package org.aayush.cache.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author Aayush Srivastava
 */

/**
 * Marks a method to be used as handler while implementing Redis distributed lock.
 *
 * <p>The method using {@link DistributedLockable} must have the first
 * argument as the key to be used for attempting Redis distributed lock.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedLockable {

    /**
     * timeout of the lock
     */
    long timeout() default 5L;

    /**
     * timeout of the lock in String
     */
    String timeoutInString() default "";

    /**
     * time unit
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * number of retries
     */
    int retries() default 0;

    /**
     * number of retries in String
     */
    String retriesInString() default "";

    /**
     * interval of each retry
     */
    long waitingTime() default 0L;

    /**
     * interval of each retry in String
     */
    String waitingTimeInString() default "";

    /**
     * key prefix
     */
    String prefix() default "";

    /**
     * parameters that construct a key
     */
    String[] argNames() default {};

    /**
     * construct a key with parameters
     */
    boolean argsAssociated() default true;

    /**
     * lock key for all other operations through out the cache
     */
    boolean permitUnrestrictedLocking() default false;

    /**
     * throw an runtime exception while fail to get lock
     */
    Class<? extends RuntimeException> onFailure() default NoException.class;

    /**
     * no exception
     */
    final class NoException extends RuntimeException {

        private static final long serialVersionUID = -7821936618527445658L;

    }
}
