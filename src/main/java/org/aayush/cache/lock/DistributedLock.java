package org.aayush.cache.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aayush Srivastava
 */
public abstract class DistributedLock implements AutoCloseable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * release lock
     */
    abstract public void release();

    @Override
    public void close() throws Exception {
        logger.debug("Distributed lock released , {}", this.toString());
        this.release();
    }
}
