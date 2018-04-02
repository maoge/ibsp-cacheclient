package ibsp.cache.client.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ibsp.cache.client.protocol.ScanParams;
import ibsp.cache.client.protocol.ScanResult;
import ibsp.cache.client.utils.Tuple;

/**
 * 缓存层API接口
 * 
 * @author liujd
 * 
 */
public interface ICacheService extends ICacheCommand, IBinaryCacheService {
	/**
	 * 设置给定key的字符串值.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return 成功返回0
	 */
	public String set(final String groupId, final String key, String value);
	
	/**
	 * 设置给定key的字符串值，并同时设置该key的超时时间，以及是否只有当该key不存在时才进行操作.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @param expireSeconds 超时时间，单位为秒，不超时则设为0
	 * @param nx 是否仅当key不存在时进行操作 
	 * @param xx 是否仅当key存在时进行操作
	 * @return 成功返回0，不成功返回-1
	 */
	public String set(final String groupId, final String key, String value, int expireSeconds, boolean nx, boolean xx);

	/**
	 * 返回给定key的值. 如果key不存在，则返回null. 如果key的类型不是string，则返回错误.
	 * 
	 * @param groupId
	 * @param key
	 * @return 返回key对应的值. 如果key不存在，则返回null. 如果key的类型不是string，则返回错误
	 */
	public String get(final String groupId, final String key);

	/**
	 * 返回给定key对应的值的长度.
	 * 
	 * @param groupId
	 * @param key
	 * @return 值的长度，如果key不存在，则返回0.
	 */
	public Long strlen(final String groupId, final String key);

	/**
	 * 向给定key的字符串末尾添加字符串.
	 * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return Integer 返回新字符串的总长度.
	 */
	public Long append(final String groupId, final String key, final String value);

	/**
	 * 将 key 所储存的值减去减量 decrement.
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内.
	 * 
	 * <p>
	 * 注意: 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * @see #incr(String, String)
	 * @see #decr(String, String)
	 * @see #incrBy(String, String, long)
	 * 
	 * @param groupId
	 * @param key
	 * @param decrement
	 * @return Integer 返回减去decrement后的新值.
	 */
	public Long decrBy(final String groupId, final String key, final long decrement);

	/**
	 * 将 key 中储存的数字值减一.
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内.
	 * 
	 * <p>
	 * 注意: 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * @see #incr(String, String)
	 * @see #incrBy(String, String, long)
	 * @see #decrBy(String, String, long)
	 * 
	 * @param groupId
	 * @param key
	 * @return Integer 返回减去1后的新值.
	 */
	public Long decr(final String groupId, final String key);

	/**
	 * 返回 key 中字符串值的子字符串，字符串的截取范围由 start 和 end 两个偏移量决定(包括 start 和 end 在内).
	 * 下标从0开始，-1 表示最后一个字符， -2 表示倒数第二个.
	 * 
	 * @param groupId
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return 截取得出的子字符串
	 */
	public byte[] getrange(final String groupId, String key, long startOffset, long endOffset);

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return 返回给定 key 的旧值，当key不存在时返回null
	 */
	public byte[] getSet(final String groupId, final String key, final String value);

	/**
	 * 将 key 中储存的数字值增一.
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内.
	 * 
	 * <p>
	 * 注意: 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * @see #incrBy(String, String, long)
	 * @see #decr(String, String)
	 * @see #decrBy(String, String, long)
	 * 
	 * @param groupId
	 * @param key
	 * @return Integer 返回增1后的新值.
	 */
	public Long incr(final String groupId, final String key);

	/**
	 * 将 key 所储存的值加上增量 increment.
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内.
	 * 
	 * <p>
	 * 注意: 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * @see #incrBy(String, String, long)
	 * @see #decr(String, String)
	 * @see #decrBy(String, String, long)
	 * 
	 * @param groupId
	 * @param key
	 * @param increment
	 * @return Integer 返回增加increment后的新值.
	 */
	public Long incrBy(final String groupId, final String key, final long increment);

	/**
	 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位).
	 * SETEX 是一个原子性(atomic)操作，关联值和设置生存时间两个动作会在同一时间内完成.
	 * 
	 * @param groupId
	 * @param key
	 * @param seconds
	 * @param value
	 * @return 设置成功时返回 OK，当 seconds 参数不合法时返回一个错误.
	 */
	public String setex(final String groupId, final String key, final int seconds, final String value);

	/**
	 * 将 key 的值设为 value ，当且仅当 key 不存在.
	 * 若给定的 key 已经存在，则 SETNX 不做任何动作.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return Integer 设置成功返回1，失败返回0.
	 */
	public Long setnx(final String groupId, final String key, final String value);

	/**
	 * 用 value 参数覆写(overwrite)给定 key 所储存的字符串值, 从偏移量 offset 开始. 
	 * 不存在的 key当作空白字符串处理.
	 * 
	 * @param groupId
	 * @param key
	 * @param offset
	 * @param value
	 * @return 返回新字符串的长度
	 */
	public Long setrange(final String groupId, String key, long offset, String value);
	// ------------------------------- list------------------------------ //
	/**
	 * 返回列表 key 中，下标为 index 的元素.
	 * 下标从0开始，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推.
	 * 
	 * @param groupId
	 * @param key
	 * @param index
	 * @return 列表中下标为 index 的元素，如果 index 参数的值不在列表的区间范围内(out of range)，返回null.
	 */
	public String lindex(final String groupId, final String key, final long index);

	/**
	 * 将值 value 插入到列表 key 当中, 位于值 pivot 之前或之后. 
	 * 当 pivot 不存在于列表 key 时, 不执行任何操作. 
	 * 当key不存在时, key被视为空列表, 不执行任何操作.
	 * 
	 * @param groupId
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return 如果命令执行成功，返回插入操作完成之后，列表的长度。如果没有找到 pivot ，返回 -1 。如果 key 不存在或为空列表，返回
	 *         0 。
	 */
	public Long linsert(final String groupId, final String key, final boolean before, final String pivot, final String value);

	/**
	 * 返回列表 key 的长度.
	 * 
	 * @param groupId
	 * @param key
	 * @return 列表长度，如果key不存在则返回0.
	 */
	public Long llen(final String groupId, final String key);

	/**
	 * 移除并返回列表 key 的头元素.
	 * 
	 * @see #lpop(String, String)
	 * 
	 * @param groupId
	 * @param key
	 * @return 列表的头元素，若key不存在则返回null
	 */
	public String lpop(final String groupId, final String key);

	/**
	 * 将一个或多个值 values插入到列表 key 的表头.
	 * 
	 * @param groupId
	 * @param key
	 * @param values
	 * @return Integer 命令执行后列表长度.
	 */
	public Long lpush(final String groupId, final String key, final String... values);

	/**
	 * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表。
	 * 和 LPUSH 命令相反，当 key 不存在时，LPUSHX 命令什么也不做。
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return LPUSHX命令执行之后，表的长度。
	 */
	public Long lpushx(final String groupId, final String key, final String... values);

	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定.
	 * 下标从0开始，-1表示最后一个元素.
	 * 
	 * @param groupId
	 * @param key
	 * @param start
	 * @param end
	 * @return 列表，包含指定区间内的元素.
	 */
	public List<byte[]> lrange(final String groupId, final String key, final long start, final long end);

	/**
	 * 根据参数 count 的值，移除列表中与参数 value 相等的元素.
	 * <p>
	 * count值有以下几种：
	 * <li>count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count.
	 * <li>count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值.
	 * <li>count = 0 : 移除表中所有与 value 相等的值.
	 * 
	 * @param groupId
	 * @param key
	 * @param count
	 * @param value
	 * @return Integer 被移除元素的数量，当key不存在时返回0.
	 */
	public Long lrem(final String groupId, final String key, final long count, final String value);

	/**
	 * 将列表 key 下标为 index 的元素的值设置为 value.
	 * <p>
	 * 当 index 参数超出范围，或对一个空列表(key 不存在)进行 LSET 时，返回一个错误.
	 * 
	 * @see #lindex(String, String, long)
	 * 
	 * @param groupId
	 * @param key
	 * @param index
	 * @param value
	 * @return 操作成功返回OK，否则返回错误信息
	 */
	public String lset(final String groupId, final String key, final long index, final String value);

	/**
	 * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除.
	 * <p>
	 * 下标从0开始，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推.
	 * 
	 * @param groupId
	 * @param key
	 * @param start
	 * @param end
	 * @return 命令执行成功时，返回 OK
	 */
	public String ltrim(final String groupId, final String key, final long start, final long end);

	/**
	 * 移除并返回列表 key 的尾元素.
	 * 
	 * @see #rpop(String, String)
	 * 
	 * @param groupId
	 * @param key
	 * @return 列表的表尾元素，当key不存在时返回null
	 */
	public String rpop(final String groupId, final String key);

	/**
	 * 将一个或多个值 values 插入到列表 key 的表尾(最右边).
	 * 
	 * @param groupId
	 * @param key
	 * @param values
	 * @return Integer 命令执行后列表的长度.
	 */
	public Long rpush(final String groupId, final String key, final String... values);

	/**
	 * 将值 value 插入到列表 key 的表尾，当且仅当 key 存在并且是一个列表。 和 RPUSH 命令相反，当 key 不存在时，
	 * RPUSHX 命令什么也不做。
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return RPUSHX命令执行之后，表的长度。
	 */
	public Long rpushx(final String groupId, final String key, final String value);

	// --------------------------------- hash ------------------------------ //
	/**
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略.
	 * 
	 * @param groupId
	 * @param key
	 * @param fields
	 * @return 被成功移除的域的数量，不包括被忽略的域.
	 */
	public Long hdel(final String groupId, final String key, final String... fields);

	/**
	 * 查看哈希表 key 中，给定域 field 是否存在.
	 * 
	 * @param groupId
	 * @param key
	 * @param field
	 * @return 如果哈希表含有给定域，返回 true.
	 *         如果哈希表不含有给定域，或 key 不存在，返回 false.
	 */
	public Boolean hexists(final String groupId, final String key, final String field);

	/**
	 * 返回哈希表 key 中给定域 field 的值.
	 * 
	 * @param groupId
	 * @param key
	 * @param field
	 * @return 给定域的值，当给定域不存在或是给定 key 不存在时返回null
	 */
	public String hget(final String groupId, final String key, final String field);

    public byte[] hget(final String groupId, final String key, final byte[] field);
	
	/**
	 * 返回哈希表 key 中，所有的域和值.
	 * 
	 * @param groupId
	 * @param key
	 * @return 返回哈希表，当key不存在时返回空哈希表.
	 */
	public Map<String, String> hgetAll(final String groupId, final String key);

    public Map<byte[], byte[]> hgetAll(final String groupId, final byte[] key);
	
	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment.
	 * <p>
	 * 本操作的值被限制在 64 位(bit)有符号数字表示之内.
	 * 
	 * @param groupId
	 * @param key
	 * @param field
	 * @param value
	 * @return Integer 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值.
	 */
	public Long hincrBy(final String groupId, final String key, final String field, final long value);

	/**
	 * 返回哈希表 key 中的所有域.
	 * 
	 * @param groupId
	 * @param key
	 * @return 一个包含哈希表中所有域的集合，当 key 不存在时，返回一个空集合.
	 */
	public Set<String> hkeys(final String groupId, final String key);

	/**
	 * 返回哈希表 key 中域的数量.
	 * 
	 * @param groupId
	 * @param key
	 * @return 哈希表中域的数量，当key不存在时返回0.
	 */
	public Long hlen(final String groupId, final String key);

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值.
	 * 如果给定的域不存在于哈希表，那么返回一个 null 值.
	 * <p>
	 * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 null 值的列表.
	 * 
	 * @param groupId
	 * @param key
	 * @param fields
	 * @return 一个包含多个给定域的关联值的列表，表值的排列顺序和给定域参数的请求顺序一样.
	 */
	public List<byte[]> hmgetBytes(final String groupId, final String key, final String... fields);

    public List<String> hmget(final String groupId, final String key, final String... fields);
	
	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中.
	 * 
	 * @param groupId
	 * @param key
	 * @param hash
	 * @return 命令执行成功，返回 OK；当key不是hash类型时返回错误.
	 */
	public String hmset(final String groupId, final String key, final Map<String, String> hash);

	/**
	 * 
	 * 将哈希表 key 中的域 field 的值设为 value.
	 * 
	 * @param groupId
	 * @param key
	 * @param field
	 * @param value
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1；如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0.
	 */
	public Long hset(final String groupId, final String key, final String field, final String value);

	/**
	 * 
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在.
	 * 
	 * @param groupId
	 * @param key
	 * @param field
	 * @param value
	 * @return 设置成功，返回 1；如果给定域已经存在且没有操作被执行，返回 0.
	 */
	public Long hsetnx(final String groupId, final String key, final String field, final String value);

	/**
	 * 返回哈希表 key 中所有域的值.
	 * 
	 * @param groupId
	 * @param key
	 * @return 一个包含哈希表中所有值的表，当key不存在时返回一个空列表.
	 */
	public List<byte[]> hvals(final String groupId, final String key);
		
    public Long sadd(String groupId, String key, String... members);
    
    public Long scard(String groupId, String key);
    
    public boolean sismember(String groupId, String key, String member);
    
    public Set<byte[]> smembers(String groupId, String key);
    
    public Long srem(String groupId, String key, String... members);
    
    public Long zadd(String groupId, String key, double score, String member);
    
    public Long zadd(String groupId, String key, Map<String, Double> scoreMembers);
    
    public Long zcard(String groupId, String key);
    
    public Long zcount(String groupId, String key, double min, double max);
    
    public double zincrby(String groupId, String key, double score, String member);
    
    public Set<String> zrange(String groupId, String key, long start, long end);
    
    public Set<Tuple> zrangeWithScores(String groupId, String key, long start, long end);
    
    public Set<String> zrangeByScore(String groupId, String key, double min, double max);
    
    public Set<String> zrangeByScore(String groupId, String key, double min, double max, int offset, int count);
    
    public Set<Tuple> zrangeByScoreWithScores(String groupId, String key, double min, double max);
    
    public Set<Tuple> zrangeByScoreWithScores(String groupId, String key, double min, double max, int offset, int count);
    
    public Long zrem(String groupId, String key, String... members);
    
    public double zscore(String groupId, String key, String member);
    
    public Long zremrangeByRank(String groupId, String key, long start, long end);
    
    public Long zremrangeByScore(String groupId, String key, long start, long end);
    
    public ScanResult<Map<byte[], byte[]>> hscan(String groupId, String key, String cursor);
    
    public ScanResult<Map<byte[], byte[]>> hscan(String groupId, String key, String cursor, ScanParams params);
        
    public void close();
}
