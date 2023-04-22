package io.naivekyo.utils;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *     Redis utility class, that contains many convenient static methods.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class RedisUtils {
    
    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);
    
    private static RedisTemplate redisTemplate;

    public RedisUtils(RedisTemplate redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

    /**
     * get RedisTemplate&lt;Object, Object&gt; instance.
     * @return {@link RedisUtils#redisTemplate}
     */
    public static RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * execute redis pipeline command.
     * @param action
     * @param <T>
     * @return
     */
    public static <T> List<T> pipelined(RedisCallback<T> action) {
        return redisTemplate.executePipelined(action);
    }

    /**
     * execute redis pipeline command without result.
     * @param action
     * @param <T>
     */
    public static <T> void pipelinedNoResult(RedisCallback<T> action) {
        redisTemplate.execute(action, false, true);
    }

    /**
     * Redis scan command
     * @param pattern the filter.
     * @param count number of elements to scan
     * @param <T>
     * @return
     */
    public static <T> Set<T> scan(String pattern, long count) {
        return (Set<T>) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Cursor<byte[]> scan = connection.scan(ScanOptions.scanOptions().count(count).match(pattern).build());
                Set<T> set = new HashSet<>();
                while (scan.hasNext()) {
                    set.add((T) scan.next());
                }
                scan.close();
                return set;
            }
        });
    }

    /**
     * Incrementally iterate the keys space. require underlying connection implementation is Lettuce. <br/>
     * TODO: use generic parameter
     * @param pattern the filter.
     * @param <T>
     * @return
     */
    public static <T> Set<T> bulkScan(String pattern) {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory == null)
            return null;
        RedisConnection connection = factory.getConnection();
        ArrayList<byte[]> keyList = null;
        try {
            keyList = new ArrayList<>();
            RedisAsyncCommands<byte[], byte[]> nativeConnection = (RedisAsyncCommands<byte[], byte[]>) connection.getNativeConnection();
            RedisCommands<byte[], byte[]> sync = nativeConnection.getStatefulConnection().sync();
            KeyScanCursor<byte[]> scanCursor = null;
            ScanArgs scanArgs = ScanArgs.Builder.limit(1000).match(pattern);
            do {
                if (scanCursor == null)
                    scanCursor = sync.scan(scanArgs);
                else
                    scanCursor = sync.scan(scanCursor, scanArgs);
                keyList.addAll(scanCursor.getKeys());
            } while (!scanCursor.isFinished());
        } catch (Exception e) {
            log.error("use scan command failure, message: {}", e.getMessage(), e);
        } finally {
            RedisConnectionUtils.releaseConnection(connection, factory);
        }
        if (keyList == null || keyList.isEmpty())
            return null;
        Set<T> keySet = new HashSet<>((int) (keyList.size() / .75f + 1f));
        for (byte[] bytes : keyList) {
            keySet.add((T) bytes);
        }
        return keySet;
    }

    // Incrementally iterate the keys space. require underlying connection implementation is Redisson. 
//	 public Set<String> bulkScan(String pattern) {
//	 	RedisConnectionFactory factory = this.redisTemplate.getConnectionFactory();
//	 	if (factory == null)
//	 		return null;
//	 	RedisConnection connection = factory.getConnection();
//	 	List<String> keyList = null;
//	 	try {
//	 		keyList = new ArrayList<>();
//	 		RedissonClient redisson = (RedissonClient) connection.getNativeConnection();
//	 		keyList = redisson.getKeys().getKeysStreamByPattern(pattern, 1000).collect(Collectors.toList());
//	 	} catch (Exception e) {
//	 		log.error("use scan command failure, message: {}", e.getMessage(), e);
//	 	} finally {
//	 		RedisConnectionUtils.releaseConnection(connection, factory);
//	 	}
//	 	if (keyList == null || keyList.isEmpty())
//	 		return null;
//	 	return new HashSet<>(keyList);
//	 }
}
