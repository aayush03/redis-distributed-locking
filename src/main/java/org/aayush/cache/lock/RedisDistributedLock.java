package org.aayush.cache.lock;

import org.aayush.cache.CacheTemplate;
import org.aayush.cache.CallbackOperation;

/**
 * @author Aayush Srivastava
 */
public class RedisDistributedLock extends DistributedLock {

    private String key;
    private String value;
    private CacheTemplate cacheTemplate;

    /**
     * @param cacheTemplate
     * @param key
     * @param value
     */
    public RedisDistributedLock(CacheTemplate cacheTemplate, String key, String value) {
        this.cacheTemplate = cacheTemplate;
        this.key = key;
        this.value = value;
    }

    @Override
    public void release() {
        //Using a lambda makes the two statements of getting value from redis and deleting it, an atomic operation
        ((CallbackOperation) () -> {
            if (value != null && cacheTemplate.getValue(key).equals(value))
                cacheTemplate.deleteValue(key);
            return null;
        }).execute(

        );
    }

    @Override
    public String toString() {
        return "RedisDistributedLock [key=" + key + ", value=" + value + "]";
    }
}
