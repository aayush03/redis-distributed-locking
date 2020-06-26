package org.aayush.cache.lock.annotation;

import org.aayush.cache.CacheTemplate;
import org.aayush.cache.impl.CacheTemplateImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author Aayush Srivastava
 */
@Aspect
@Component
public class DistributedLockableAspect<T> implements KeyGenerator {

    private Logger logger = LoggerFactory.getLogger(CacheTemplateImpl.class);
    @Autowired
    private CacheTemplate redisLockClient;
    @Autowired
    private Environment environment;

    /**
     * {@link DistributedLockable}
     */
    @Pointcut(value = "execution(* *(..)) && @annotation(org.aayush.cache.lock.annotation.DistributedLockable)")
    public void distributedLockable() {
        logger.info("POINTCUT ACTIVATED");
    }

    /**
     * @param joinPoint
     * @param lockable
     * @return
     * @throws Throwable
     */
    @Around(value = "distributedLockable() && @annotation(lockable)")
    public Object handle(ProceedingJoinPoint joinPoint, DistributedLockable lockable) throws Throwable {
        long start = System.nanoTime();
        String key = (String) joinPoint.getArgs()[0];
        if (lockable.permitUnrestrictedLocking())
            key = this.generate(joinPoint, getCurrentMethod(joinPoint), lockable.prefix(), lockable.argNames(), lockable.argsAssociated()).toString();

        long timeOutVal = parseLongWithDefaultValue(environment.getProperty(lockable.timeoutInString()), lockable.timeout());
        long waitingTimeVal = parseLongWithDefaultValue(environment.getProperty(lockable.waitingTimeInString()), lockable.waitingTime());
        int retryVal = parseIntWithDefaultValue(environment.getProperty(lockable.retriesInString()), lockable.retries());

        logger.debug("Attempting to acquire distributed lock for key : {} with timeout : {} waitingTime : {} retryCount : {}", key, timeOutVal, waitingTimeVal, retryVal);
        T result = (T) redisLockClient.tryLock(
                key, () -> {
                    return joinPoint.proceed();
                },
                lockable.unit().toMillis(timeOutVal),
                retryVal, lockable.unit().toMillis(waitingTimeVal),
                lockable.onFailure()
        );
        long end = System.nanoTime();
        logger.info("distributed lockable cost: {} ns", end - start);
        return result;
    }

    private long parseLongWithDefaultValue(String stringToBeParsed, long defaultValue) {
        long val;
        try {
            val = Long.parseLong(stringToBeParsed);
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        return val;
    }

    private int parseIntWithDefaultValue(String stringToBeParsed, int defaultValue) {
        int val;
        try {
            val = Integer.parseInt(stringToBeParsed);
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        return val;
    }

    private Method getCurrentMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return target.getClass().getSimpleName() + "_"
                + method.getName() + "_"
                + StringUtils.arrayToDelimitedString(params, "_");
    }
}
