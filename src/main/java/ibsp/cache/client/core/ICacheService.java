package ibsp.cache.client.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ibsp.cache.client.protocol.Tuple;

public interface ICacheService extends ICacheCommand, IBinaryCacheService {

	public String set(final String key, String value);

	public String set(final String key, String value, int expireSeconds, boolean nx, boolean xx);

	public String get(final String key);

	public Long strlen(final String key);

	public Long append(final String key, final String value);

	public Long decrBy(final String key, final long decrement);

	public Long decr(final String key);

	public byte[] getrange(String key, long startOffset, long endOffset);

	public byte[] getSet(final String key, final String value);

	public Long incr(final String key);

	public Long incrBy(final String key, final long increment);

	public String setex(final String key, final int seconds, final String value);

	public Long setnx(final String key, final String value);

	public Long setrange(String key, long offset, String value);

	public String lindex(final String key, final long index);

	public Long linsert(final String key, final boolean before, final String pivot, final String value);

	public Long llen(final String key);

	public byte[] lpop(final String key);

	public Long lpush(final String key, final byte[]... values);

	public Long lpushx(final String key, final byte[]... values);

	public List<byte[]> lrange(final String key, final long start, final long end);

	public Long lrem(final String key, final long count, final String value);

	public String lset(final String key, final long index, final String value);

	public String ltrim(final String key, final long start, final long end);

	public byte[] rpop(final String key);
	
	public List<byte[]> brpop(int timeout, final byte[]... keys);

	public Long rpush(final String key, final byte[]... values);

	public Long rpushx(final String key, final String value);

	public Long hdel(final String key, final String... fields);

	public Boolean hexists(final String key, final String field);

	public String hget(final String key, final String field);

	public byte[] hget(final String key, final byte[] field);

	public Map<String, String> hgetAll(final String key);

	public Map<byte[], byte[]> hgetAll(final byte[] key);

	public Long hincrBy(final String key, final String field, final long value);

	public Set<String> hkeys(final String key);

	public Long hlen(final String key);

	public List<byte[]> hmgetBytes(final String key, final String... fields);

	public List<String> hmget(final String key, final String... fields);

	public String hmset(final String key, final Map<String, String> hash);

	public Long hset(final String key, final String field, final String value);

	public Long hsetnx(final String key, final String field, final String value);

	public List<byte[]> hvals(final String key);

	public Long sadd(String key, String... members);

	public Long scard(String key);

	public boolean sismember(String key, String member);

	public Set<byte[]> smembers(String key);

	public Long srem(String key, String... members);

	public Long zadd(String key, double score, String member);

	public Long zadd(String key, Map<String, Double> scoreMembers);

	public Long zcard(String key);

	public Long zcount(String key, double min, double max);

	public double zincrby(String key, double score, String member);

	public Set<String> zrange(String key, long start, long end);

	public Set<Tuple> zrangeWithScores(String key, long start, long end);

	public Set<String> zrangeByScore(String key, double min, double max);

	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

	public Long zrem(String key, String... members);

	public double zscore(String key, String member);

	public Long zremrangeByRank(String key, long start, long end);

	public Long zremrangeByScore(String key, long start, long end);

	public void close();

}
