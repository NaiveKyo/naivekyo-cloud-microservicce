package io.naivekyo.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *     twitter snowflake algorithm implementation.
 * </p>
 * <p>
 *     see https://github.com/twitter-archive/snowflake <br/>
 *     see https://www.cnblogs.com/relucent/p/4955340.html
 * </p>
 * <p>
 *     Snowflake have 64 bit, we use 63 bit and remain 1 bit as as sign bit. <br />
 *     Meanwhile we expect generate positive id number, so the sign bit is always 0. <br />
 *     Of course you can customize it in your own special implementation.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
public class SnowflakeGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(SnowflakeGenerator.class);
    
    private static volatile SnowflakeAlgoImpl snowflakeGenerator = null;

    private SnowflakeGenerator() {
    }

    /**
     * you must call this method once before consume snowflake-id
     * @param datacenterId
     * @param machineId
     */
    public static void initGenerator(long datacenterId, long machineId) {
        if (snowflakeGenerator == null) {
            synchronized (SnowflakeAlgoImpl.class) {
                if (snowflakeGenerator == null) {
                    snowflakeGenerator = new SnowflakeAlgoImpl(datacenterId, machineId);
                    log.info("snowflake generator initialize successful.");
                }
            }
        } else {
            log.warn("snowflake generator was already initialized, you don't need to repeat call init method.");
        }
    }

    /**
     * get distributed snowflake id.
     * @return
     */
    public static long getNextId() {
        if (snowflakeGenerator == null)
            throw new RuntimeException("snowflake generator has not been initialized!");
        return snowflakeGenerator.nextId();
    }

    /**
     * get distributed snowflake id in a test environment.
     * @return
     */
    public static long getNextIdTest() {
        if (snowflakeGenerator == null)
            throw new RuntimeException("snowflake generator has not been initialized!");
        return snowflakeGenerator.nextIdLowQPS();
    }
    
    public static class SnowflakeAlgoImpl {
        
        // begin time(UTC): 2020-01-01T00:00:00+08:00
        static final long START_STAMP = 1577836800000L;
        
        // ======================= 10 bit work-id split into two parts =======================
        // 5 bit datacenter-id
        static final long DATACENTER_ID_BIT = 5L;
        
        // 5 bit machine-id
        static final long MACHINE_ID_BIT = 5L;
        
        // support maximum machine-id: 2^5 - 1 = 31
        static final long MAX_MACHINE_ID = -1L ^ (-1L << MACHINE_ID_BIT);
        
        // support maximum datacenter-id: 2^5 - 1 = 31
        static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BIT);
        
        // ======================= 12 bit sequence number =======================
        static final long SEQUENCE_BIT = 12L;
        
        // support maximum sequence number: 2^12 - 1 = 4095
        static final long MAX_SEQUENCE_NUMBER = -1L ^ (-1L << SEQUENCE_BIT);
        
        // ======================= 41 bit timestamp =======================
        static final long TIMESTAMP_BIT = 41L;

        // snowflake id: ${timestamp}(${datacenter-id}${machine-id})${sequence-number}
        //                    41             5              5               12
        // machine-id left offset: 12
        static final long MACHINE_ID_OFFSET = SEQUENCE_BIT;
        // datacenter-id left offset: 12 + 5
        static final long DATACENTER_ID_OFFSET = SEQUENCE_BIT + MACHINE_ID_BIT;
        // timestamp left offset: 12 + 5 + 5
        static final long TIMESTAMP_OFFSET = SEQUENCE_BIT + MACHINE_ID_BIT + DATACENTER_ID_BIT;
        
        // ========================= properties ==========================
        long datacenterId = 0L;
        long machineId = 0L;
        long sequence = 0L;
        long lastTimestamp = -1L;

        public SnowflakeAlgoImpl(long datacenterId, long machineId) {
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0L)
                throw new IllegalArgumentException(String.format("datacenterId cannot be greater than %d or less than 0", MAX_DATACENTER_ID));
            if (machineId > MAX_MACHINE_ID || machineId < 0L)
                throw new IllegalArgumentException(String.format("machineId cannot be greater than %d or less than 0", MAX_MACHINE_ID));
            
            this.datacenterId = datacenterId;
            this.machineId = machineId;
        }

        /**
         * get next snowflake id.
         * @return snowflake-id
         */
        public synchronized long nextId() {
            long timestamp = this.getCurrentTimestamp();
            // 如果当前时间戳小于上一次生成 ID 时使用的时间戳, 说明系统时间回退过, 需要抛出异常
            if (timestamp < lastTimestamp)
                throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));

            // 如果是同一毫秒生成的, 则获取可用序列号
            if (timestamp == lastTimestamp) {
                this.sequence = (sequence + 1) & MAX_SEQUENCE_NUMBER;
                if (sequence == 0) {
                    // 当前毫秒无可用序列号, 等待下一毫秒
                    timestamp = this.waitNextMillis(timestamp);
                }
            } else {
                // 为下一毫秒重置序列号
                sequence = 0L;
            }

            this.lastTimestamp = timestamp;

            // 利用位运算计算完整的雪花 id
            return ((timestamp - START_STAMP) << TIMESTAMP_OFFSET)
                | (datacenterId << DATACENTER_ID_OFFSET)
                | (machineId << MACHINE_ID_OFFSET)
                | sequence;
        }

        /**
         * <p>
         *     recommend use this method to acquire snowflake-id in test environment. <br/>
         *     since it can ensure snowflake-id have a uniform distribution with simple mod hash function.
         * </p>
         * @return
         */
        public synchronized long nextIdLowQPS() {
            long timestamp = this.getCurrentTimestamp();
            // 如果当前时间戳小于上一次生成 ID 时使用的时间戳, 说明系统时间回退过, 需要抛出异常
            if (timestamp < lastTimestamp)
                throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));

            // 如果是同一毫秒生成的, 则获取可用序列号
            if (timestamp == lastTimestamp) {
                this.sequence = (sequence + 1) & MAX_SEQUENCE_NUMBER;
                if (sequence == 0) {
                    // 当前毫秒无可用序列号, 等待下一毫秒
                    timestamp = this.waitNextMillis(timestamp);
                }
            } else {
                // 多毫秒内无需重置, 持续递增
                sequence = (sequence + 1) & MAX_SEQUENCE_NUMBER;
            }

            this.lastTimestamp = timestamp;

            // 利用位运算计算完整的雪花 id
            return ((timestamp - START_STAMP) << TIMESTAMP_OFFSET)
                    | (datacenterId << DATACENTER_ID_OFFSET)
                    | (machineId << MACHINE_ID_OFFSET)
                    | sequence;
        }
        

        /**
         * An extensible way to get a system timestamp. <br/>
         * e.g. you can use a schedule task to maintain a time-counter to avoid interact with the underlying system.
         * @return current system timestamp
         */
        protected long getCurrentTimestamp() {
            return System.currentTimeMillis();
        }

        protected long waitNextMillis(long currentTimestamp) {
            while (currentTimestamp == lastTimestamp)
                currentTimestamp = this.getCurrentTimestamp();
            return currentTimestamp;
        }

    }
}
