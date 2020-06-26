package org.aayush.service;

import org.aayush.cache.lock.annotation.DistributedLockable;
import org.aayush.model.MockModel;
import org.springframework.stereotype.Service;

/**
 * @author Aayush Srivastava
 */
@Service
public class MockServiceImpl implements MockService {

    @Override
    @DistributedLockable(
            waitingTimeInString = "redis.distributed.locking.waitingTimeInMilliSeconds",
            timeoutInString = "redis.distributed.locking.timeoutInMilliSeconds",
            retriesInString = "redis.distributed.locking.retry.count"
    )
    public String getMockValue(String transactionId) {
        return getMockedString(transactionId, new MockModel(1, "Aayush Srivastava"));
    }


    private String getMockedString(String transactionId, MockModel model) {
        StringBuilder builder = new StringBuilder();
        builder.append(transactionId)
                .append("::")
                .append(model);
        return builder.toString();
    }
}
