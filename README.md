# redis-distributed-locking
This repository contains a sample application to implement distributed locking using Redis along with support of using configurable environmental properties for parameterization of the DistributedLockable Annotation.

This has been built on top of the original article published on https://blog.piaoruiqing.com/

This can be used to acquire distributed locks through Redis over a specific key across all sentinels using the SETNX command implementation of Redis and uses Java lambdas to combine multiple statements of releasing the distributed lock and converts this process into an atomic operation and ensures correctness of the unlocking operation by not deleting someone else's lock.

Reference : https://redis.io/commands/setnx | https://developpaper.com/redis-distributed-lock/
