package org.aayush.cache.impl;

import org.aayush.cache.CacheTemplate;
import org.aayush.cache.lock.DistributedLock;
import org.aayush.cache.lock.LockHandler;
import org.aayush.cache.lock.RedisDistributedLock;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Aayush Srivastava
 */
@Service("cacheTemplate")
public class CacheTemplateImpl implements CacheTemplate {

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    private final String PREFIX = ":";

    private Logger logger = LoggerFactory.getLogger(CacheTemplateImpl.class);


    public void putValue(final String key, final Object value, final String cacheName) {
        putValue(concatenateCacheNameWithKey(key, cacheName), value);
    }

    public void putValues(final String key, final List<Object> value, final String cacheName) {
        putValue(key, value, cacheName);
    }

    public void putValue(final String key, final Object value, final String cacheName, long time, final TimeUnit unit) {
        putValue(concatenateCacheNameWithKey(key, cacheName), value, time, unit);
    }

    public void putValue(final String key, final Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception ex) {
            logger.error("Exception::", ex);
        }
    }

    public void putValue(final String key, final Object value, long time, final TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, time, unit);
        } catch (Exception ex) {
            logger.error("Exception::", ex);
        }
    }

    @Override
    public Object getValue(final String key, final String cacheName) {
        return getValue(concatenateCacheNameWithKey(key, cacheName));
    }

    public Object getValue(final String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            logger.error("Exception::", ex);
        }
        return null;
    }

    public void deleteKeys(List<String> keys, String cacheName) {
        logger.info("bulk deletion of redis keys : {}", keys);
        keys.stream().forEach(k -> deleteValue(k, cacheName));
    }

    @Override
    public void deleteValue(final String key, final String cacheName) {
        deleteValue(concatenateCacheNameWithKey(key, cacheName));
    }

    public void deleteValue(final String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            logger.error("Exception::", ex);
        }
    }

    @Override
    public void clearAllCache(final String cacheName) {
        try {
            redisTemplate.keys(getCacheNamePattern(cacheName)).stream().forEach(k -> deleteValue((String) k));
        } catch (Exception ex) {
            logger.error("Exception::", ex);
        }
    }

    private String concatenateCacheNameWithKey(final String key, final String cacheName) {
        return cacheName + PREFIX + key;
    }

    private String getCacheNamePattern(final String cacheName) {
        return cacheName + PREFIX + "*";
    }

    public Boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    public <T> T tryLock(String key, LockHandler<T> handler, long timeout, int retries, long waitingTime) throws Throwable {
        try (DistributedLock lock = this.acquireLock(key, timeout, retries, waitingTime);) {
            if (lock != null) {
                logger.debug("get lock success, key: {}", key);
                return handler.handle();
            }
            logger.debug("get lock failed, key: {}", key);
            return null;
        }
    }

    public <T> T tryLock(String key, LockHandler<T> handler, long timeout, int retries, long waitingTime, Class<? extends RuntimeException> onFailure) throws Throwable {
        try (DistributedLock lock = this.acquireLock(key, timeout, retries, waitingTime)) {
            if (lock != null) {
                logger.debug("get lock success, key: {}", key);
                return handler.handle();
            }
            logger.debug("get lock failed, key: {}", key);
            if (null != onFailure) {
                throw onFailure.newInstance(); // (2)
            }
            return null;
        }
    }

    private DistributedLock acquireLock(String key, long timeout, int retries, long waitingTime) throws InterruptedException {
        final String value
                = RandomStringUtils.randomAlphanumeric(4) + System.currentTimeMillis();
        do {
            Boolean result
                    = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
            if (result) {
                return new RedisDistributedLock(this, key, value);
            }
            if (retries > NumberUtils.INTEGER_ZERO) {
                TimeUnit.MILLISECONDS.sleep(waitingTime);
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        } while (retries-- > NumberUtils.INTEGER_ZERO);

        return null;
    }
}