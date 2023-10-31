package com.tangwuji.reggie.utils;

public class SnowflakeIdGenerator {
    private static final long START_TIMESTAMP = 1672502400L; // 设置起始时间戳，如2023-01-01 00:00:00
    private static final long WORKER_ID_BITS = 5L; // 机器ID所占的位数
    private static final long DATA_CENTER_ID_BITS = 5L; // 数据中心ID所占的位数
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 最大机器ID
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS); // 最大数据中心ID
    private static final long SEQUENCE_BITS = 12L; // 序列号所占的位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS; // 机器ID左移位数
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // 数据中心ID左移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS; // 时间戳左移位数
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS); // 序列号的掩码
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;
    private final long workerId;
    private final long dataCenterId;

    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID超出范围");
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("Data Center ID超出范围");
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨异常");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
