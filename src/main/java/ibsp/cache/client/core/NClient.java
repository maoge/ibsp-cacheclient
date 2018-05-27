package ibsp.cache.client.core;

import static ibsp.cache.client.protocol.Protocol.toByteArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ibsp.cache.client.command.BinaryClient.LIST_POSITION;
import ibsp.cache.client.protocol.BitOP;
import ibsp.cache.client.protocol.BitPosParams;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.protocol.SortingParams;
import ibsp.cache.client.protocol.ZParams;

public class NClient extends NBinaryClient  {
    public NClient(String host, int port, int timeOut, String connectionName, boolean isSync)  {
        super(host, port, timeOut, connectionName, isSync);
    }
    
	public NClient(String host, int port, int timeOut, boolean isSync)  {
		super(host, port, timeOut, isSync);
	}

	public NClient(String host, int port, boolean isSync){
		super(host, port, isSync);
	}
	
	public String getConnname() {
		return this.connection.getConnname();
	}

	public byte[] set(final String key, final String value) {
		return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
	}

	public byte[] set(final String key, final String value,
			final String nxxx, final String expx, final long time) {
		return set(SafeEncoder.encode(key), SafeEncoder.encode(value),
				SafeEncoder.encode(nxxx), SafeEncoder.encode(expx), time);
	}

	public byte[] get(final String key) {
		return get(SafeEncoder.encode(key));
	}

	public byte[] exists(final String key) {
		return exists(SafeEncoder.encode(key));
	}

	public byte[] del(final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < keys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return del(bkeys);
	}

	public byte[] type(final String key) {
		return type(SafeEncoder.encode(key));
	}

	public byte[] keys(final String pattern) {
		return keys(SafeEncoder.encode(pattern));
	}

	public byte[] rename(final String oldkey,
			final String newkey) {
		return rename(SafeEncoder.encode(oldkey), SafeEncoder.encode(newkey));
	}

	public byte[] renamenx(final String oldkey,
			final String newkey) {
		return renamenx(SafeEncoder.encode(oldkey), SafeEncoder.encode(newkey));
	}

	public byte[] expire(final String key,
			final int seconds) {
		return expire(SafeEncoder.encode(key), seconds);
	}

	public byte[] expireAt(final String key,
			final long unixTime) {
		return expireAt(SafeEncoder.encode(key), unixTime);
	}

	public byte[] ttl(final String key) {
		return ttl(SafeEncoder.encode(key));
	}

	public byte[] move(final String key, final int dbIndex) {
		return move(SafeEncoder.encode(key), dbIndex);
	}

	public byte[] getSet(final String key,
			final String value) {
		return getSet(SafeEncoder.encode(key), SafeEncoder.encode(value));
	}

	public byte[] mget(final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return mget(bkeys);
	}

	public byte[] setnx(final String key,
			final String value) {
		return setnx(SafeEncoder.encode(key), SafeEncoder.encode(value));
	}

	public byte[] setex(final String key, final int seconds,
			final String value) {
		return setex(SafeEncoder.encode(key), seconds,
				SafeEncoder.encode(value));
	}

	public byte[] mset(final String... keysvalues) {
		final byte[][] bkeysvalues = new byte[keysvalues.length][];
		for (int i = 0; i < keysvalues.length; i++) {
			bkeysvalues[i] = SafeEncoder.encode(keysvalues[i]);
		}
		return mset(bkeysvalues);
	}

	public byte[] msetnx(final String... keysvalues) {
		final byte[][] bkeysvalues = new byte[keysvalues.length][];
		for (int i = 0; i < keysvalues.length; i++) {
			bkeysvalues[i] = SafeEncoder.encode(keysvalues[i]);
		}
		return msetnx(bkeysvalues);
	}

	public byte[] decrBy(final String key,
			final long integer) {
		return decrBy(SafeEncoder.encode(key), integer);
	}

	public byte[] decr(final String key) {
		return decr(SafeEncoder.encode(key));
	}

	public byte[] incrBy(final String key,
			final long integer) {
		return incrBy(SafeEncoder.encode(key), integer);
	}

	public byte[] incr(final String key) {
		return incr(SafeEncoder.encode(key));
	}

	public byte[] append(final String key,
			final String value) {
		return append(SafeEncoder.encode(key), SafeEncoder.encode(value));
	}

	public byte[] substr(final String key, final int start,
			final int end) {
		return substr(SafeEncoder.encode(key), start, end);
	}

	public byte[] hset(final String key, final String field,
			final String value) {
		return hset(SafeEncoder.encode(key), SafeEncoder.encode(field),
				SafeEncoder.encode(value));
	}

	public byte[] hget(final String key, final String field) {
		return hget(SafeEncoder.encode(key), SafeEncoder.encode(field));
	}

	public byte[] hsetnx(final String key, final String field,
			final String value) {
		return hsetnx(SafeEncoder.encode(key), SafeEncoder.encode(field),
				SafeEncoder.encode(value));
	}

	public byte[] hmset(final String key,
			final Map<String, String> hash) {
		final Map<byte[], byte[]> bhash = new HashMap<byte[], byte[]>(
				hash.size());
		for (final Entry<String, String> entry : hash.entrySet()) {
			bhash.put(SafeEncoder.encode(entry.getKey()),
					SafeEncoder.encode(entry.getValue()));
		}
		return hmset(SafeEncoder.encode(key), bhash);
	}

	public byte[] hmget(final String key,
			final String... fields) {
		final byte[][] bfields = new byte[fields.length][];
		for (int i = 0; i < bfields.length; i++) {
			bfields[i] = SafeEncoder.encode(fields[i]);
		}
		return hmget(SafeEncoder.encode(key), bfields);
	}

	public byte[] hincrBy(final String key,
			final String field, final long value) {
		return hincrBy(SafeEncoder.encode(key), SafeEncoder.encode(field),
				value);
	}

	public byte[] hexists(final String key,
			final String field) {
		return hexists(SafeEncoder.encode(key), SafeEncoder.encode(field));
	}

	public byte[] hdel(final String key,
			final String... fields) {
		return hdel(SafeEncoder.encode(key), SafeEncoder.encodeMany(fields));
	}

	public byte[] hlen(final String key) {
		return hlen(SafeEncoder.encode(key));
	}

	public byte[] hkeys(final String key) {
		return hkeys(SafeEncoder.encode(key));
	}

	public byte[] hvals(final String key) {
		return hvals(SafeEncoder.encode(key));
	}

	public byte[] hgetAll(final String key) {
		return hgetAll(SafeEncoder.encode(key));
	}

	public byte[] rpush(final String key,
			final String... string) {
		return rpush(SafeEncoder.encode(key), SafeEncoder.encodeMany(string));
	}

	public byte[] lpush(final String key,
			final String... string) {
		return lpush(SafeEncoder.encode(key), SafeEncoder.encodeMany(string));
	}

	public byte[] llen(final String key) {
		return llen(SafeEncoder.encode(key));
	}

	public byte[] lrange(final String key, final long start,
			final long end) {
		return lrange(SafeEncoder.encode(key), start, end);
	}

	public byte[] ltrim(final String key, final long start,
			final long end) {
		return ltrim(SafeEncoder.encode(key), start, end);
	}

	public byte[] lindex(final String key, final long index) {
		return lindex(SafeEncoder.encode(key), index);
	}

	public byte[] lset(final String key, final long index,
			final String value) {
		return lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
	}

	public byte[] lrem(final String key, long count,
			final String value) {
		return lrem(SafeEncoder.encode(key), count, SafeEncoder.encode(value));
	}

	public byte[] lpop(final String key) {
		return lpop(SafeEncoder.encode(key));
	}

	public byte[] rpop(final String key) {
		return rpop(SafeEncoder.encode(key));
	}

	public byte[] rpoplpush(final String srckey,
			final String dstkey) {
		return rpoplpush(SafeEncoder.encode(srckey),
				SafeEncoder.encode(dstkey));
	}

	public byte[] sadd(final String key,
			final String... members) {
		return sadd(SafeEncoder.encode(key), SafeEncoder.encodeMany(members));
	}

	public byte[] smembers(final String key) {
		return smembers(SafeEncoder.encode(key));
	}

	public byte[] srem(final String key,
			final String... members) {
		return srem(SafeEncoder.encode(key), SafeEncoder.encodeMany(members));
	}

	public byte[] spop(final String key) {
		return spop(SafeEncoder.encode(key));
	}

	public byte[] spop(final String key, final long count) {
		return spop(SafeEncoder.encode(key), count);
	}

	public byte[] smove(final String srckey,
			final String dstkey, final String member) {
		return smove(SafeEncoder.encode(srckey), SafeEncoder.encode(dstkey),
				SafeEncoder.encode(member));
	}

	public byte[] scard(final String key) {
		return scard(SafeEncoder.encode(key));
	}

	public byte[] sismember(final String key,
			final String member) {
		return sismember(SafeEncoder.encode(key), SafeEncoder.encode(member));
	}

	public byte[] sinter(final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return sinter(bkeys);
	}

	public byte[] sinterstore(final String dstkey,
			final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return sinterstore(SafeEncoder.encode(dstkey), bkeys);
	}

	public byte[] sunion(final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return sunion(bkeys);
	}

	public byte[] sunionstore(final String dstkey,
			final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return sunionstore(SafeEncoder.encode(dstkey), bkeys);
	}

	public byte[] sdiff(final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return sdiff(bkeys);
	}

	public byte[] sdiffstore(final String dstkey,
			final String... keys) {
		final byte[][] bkeys = new byte[keys.length][];
		for (int i = 0; i < bkeys.length; i++) {
			bkeys[i] = SafeEncoder.encode(keys[i]);
		}
		return sdiffstore(SafeEncoder.encode(dstkey), bkeys);
	}

	public byte[] srandmember(final String key) {
		return srandmember(SafeEncoder.encode(key));
	}

	public byte[] zadd(final String key, final double score,
			final String member) {
		return zadd(SafeEncoder.encode(key), score, SafeEncoder.encode(member));
	}

	public byte[] zrange(final String key, final long start,
			final long end) {
		return zrange(SafeEncoder.encode(key), start, end);
	}

	public byte[] zrem(final String key,
			final String... members) {
		return zrem(SafeEncoder.encode(key), SafeEncoder.encodeMany(members));
	}

	public byte[] zincrby(final String key,
			final double score, final String member) {
		return zincrby(SafeEncoder.encode(key), score,
				SafeEncoder.encode(member));
	}

	public byte[] zrank(final String key,
			final String member) {
		return zrank(SafeEncoder.encode(key), SafeEncoder.encode(member));
	}

	public byte[] zrevrank(final String key,
			final String member) {
		return zrevrank(SafeEncoder.encode(key), SafeEncoder.encode(member));
	}

	public byte[] zrevrange(final String key,
			final long start, final long end) {
		return zrevrange(SafeEncoder.encode(key), start, end);
	}

	public byte[] zrangeWithScores(final String key,
			final long start, final long end) {
		return zrangeWithScores(SafeEncoder.encode(key), start, end);
	}

	public byte[] zrevrangeWithScores(final String key,
			final long start, final long end) {
		return zrevrangeWithScores(SafeEncoder.encode(key), start, end);
	}

	public byte[] zcard(final String key) {
		return zcard(SafeEncoder.encode(key));
	}

	public byte[] zscore(final String key,
			final String member) {
		return zscore(SafeEncoder.encode(key), SafeEncoder.encode(member));
	}

	public byte[] watch(final String... keys) {
		final byte[][] bargs = new byte[keys.length][];
		for (int i = 0; i < bargs.length; i++) {
			bargs[i] = SafeEncoder.encode(keys[i]);
		}
		return watch(bargs);
	}

	public byte[] sort(final String key) {
		return sort(SafeEncoder.encode(key));
	}

	public byte[] sort(final String key,
			final SortingParams sortingParameters) {
		return sort(SafeEncoder.encode(key), sortingParameters);
	}

	public byte[] blpop(final String[] args) {
		final byte[][] bargs = new byte[args.length][];
		for (int i = 0; i < bargs.length; i++) {
			bargs[i] = SafeEncoder.encode(args[i]);
		}
		return blpop(bargs);
	}

	public byte[] blpop(final int timeout,
			final String... keys) {
		final int size = keys.length + 1;
		List<String> args = new ArrayList<String>(size);
		for (String arg : keys) {
			args.add(arg);
		}
		args.add(String.valueOf(timeout));
		return blpop(args.toArray(new String[size]));
	}

	public byte[] sort(final String key,
			final SortingParams sortingParameters, final String dstkey) {
		return sort(SafeEncoder.encode(key), sortingParameters,
				SafeEncoder.encode(dstkey));
	}

	public byte[] sort(final String key,
			final String dstkey) {
		return sort(SafeEncoder.encode(key), SafeEncoder.encode(dstkey));
	}

	public byte[] brpop(final String[] args) {
		final byte[][] bargs = new byte[args.length][];
		for (int i = 0; i < bargs.length; i++) {
			bargs[i] = SafeEncoder.encode(args[i]);
		}
		return brpop(bargs);
	}

	public byte[] brpop(final int timeout,
			final String... keys) {
		final int size = keys.length + 1;
		List<String> args = new ArrayList<String>(size);
		for (String arg : keys) {
			args.add(arg);
		}
		args.add(String.valueOf(timeout));
		return brpop(args.toArray(new String[size]));
	}

	public byte[] zcount(final String key, final double min,
			final double max) {
		return zcount(SafeEncoder.encode(key), toByteArray(min),
				toByteArray(max));
	}

	public byte[] zcount(final String key, final String min,
			final String max) {
		return zcount(SafeEncoder.encode(key), SafeEncoder.encode(min),
				SafeEncoder.encode(max));
	}

	public byte[] zrangeByScore(final String key,
			final double min, final double max) {
		return zrangeByScore(SafeEncoder.encode(key), toByteArray(min),
				toByteArray(max));
	}

	public byte[] zrangeByScore(final String key,
			final String min, final String max) {
		return zrangeByScore(SafeEncoder.encode(key), SafeEncoder.encode(min),
				SafeEncoder.encode(max));
	}

	public byte[] zrangeByScore(final String key,
			final double min, final double max, final int offset, int count) {
		return zrangeByScore(SafeEncoder.encode(key), toByteArray(min),
				toByteArray(max), offset, count);
	}

	public byte[] zrangeByScoreWithScores(final String key,
			final double min, final double max) {
		return zrangeByScoreWithScores(SafeEncoder.encode(key),
				toByteArray(min), toByteArray(max));
	}

	public byte[] zrangeByScoreWithScores(final String key,
			final double min, final double max, final int offset,
			final int count) {
		return zrangeByScoreWithScores(SafeEncoder.encode(key),
				toByteArray(min), toByteArray(max), offset, count);
	}

	public byte[] zrevrangeByScore(final String key,
			final double max, final double min) {
		return zrevrangeByScore(SafeEncoder.encode(key), toByteArray(max),
				toByteArray(min));
	}

	public byte[] zrangeByScore(final String key,
			final String min, final String max, final int offset, int count) {
		return zrangeByScore(SafeEncoder.encode(key), SafeEncoder.encode(min),
				SafeEncoder.encode(max), offset, count);
	}

	public byte[] zrangeByScoreWithScores(final String key,
			final String min, final String max) {
		return zrangeByScoreWithScores(SafeEncoder.encode(key),
				SafeEncoder.encode(min), SafeEncoder.encode(max));
	}

	public byte[] zrangeByScoreWithScores(final String key,
			final String min, final String max, final int offset,
			final int count) {
		return zrangeByScoreWithScores(SafeEncoder.encode(key),
				SafeEncoder.encode(min), SafeEncoder.encode(max), offset,
				count);
	}

	public byte[] zrevrangeByScore(final String key,
			final String max, final String min) {
		return zrevrangeByScore(SafeEncoder.encode(key),
				SafeEncoder.encode(max), SafeEncoder.encode(min));
	}

	public byte[] zrevrangeByScore(final String key,
			final double max, final double min, final int offset, int count) {
		return zrevrangeByScore(SafeEncoder.encode(key), toByteArray(max),
				toByteArray(min), offset, count);
	}

	public byte[] zrevrangeByScore(final String key,
			final String max, final String min, final int offset, int count) {
		return zrevrangeByScore(SafeEncoder.encode(key),
				SafeEncoder.encode(max), SafeEncoder.encode(min), offset,
				count);
	}

	public byte[] zrevrangeByScoreWithScores(final String key,
			final double max, final double min) {
		return zrevrangeByScoreWithScores(SafeEncoder.encode(key),
				toByteArray(max), toByteArray(min));
	}

	public byte[] zrevrangeByScoreWithScores(final String key,
			final String max, final String min) {
		return zrevrangeByScoreWithScores(SafeEncoder.encode(key),
				SafeEncoder.encode(max), SafeEncoder.encode(min));
	}

	public byte[] zrevrangeByScoreWithScores(final String key,
			final double max, final double min, final int offset,
			final int count) {
		return zrevrangeByScoreWithScores(SafeEncoder.encode(key),
				toByteArray(max), toByteArray(min), offset, count);
	}

	public byte[] zrevrangeByScoreWithScores(final String key,
			final String max, final String min, final int offset,
			final int count) {
		return zrevrangeByScoreWithScores(SafeEncoder.encode(key),
				SafeEncoder.encode(max), SafeEncoder.encode(min), offset,
				count);
	}

	public byte[] zremrangeByRank(final String key,
			final long start, final long end) {
		return zremrangeByRank(SafeEncoder.encode(key), start, end);
	}

	public byte[] zremrangeByScore(final String key,
			final double start, final double end) {
		return zremrangeByScore(SafeEncoder.encode(key), toByteArray(start),
				toByteArray(end));
	}

	public byte[] zremrangeByScore(final String key,
			final String start, final String end) {
		return zremrangeByScore(SafeEncoder.encode(key),
				SafeEncoder.encode(start), SafeEncoder.encode(end));
	}

	public byte[] zunionstore(final String dstkey,
			final String... sets) {
		final byte[][] bsets = new byte[sets.length][];
		for (int i = 0; i < bsets.length; i++) {
			bsets[i] = SafeEncoder.encode(sets[i]);
		}
		return zunionstore(SafeEncoder.encode(dstkey), bsets);
	}

	public byte[] zunionstore(final String dstkey,
			final ZParams params, final String... sets) {
		final byte[][] bsets = new byte[sets.length][];
		for (int i = 0; i < bsets.length; i++) {
			bsets[i] = SafeEncoder.encode(sets[i]);
		}
		return zunionstore(SafeEncoder.encode(dstkey), params, bsets);
	}

	public byte[] zinterstore(final String dstkey,
			final String... sets) {
		final byte[][] bsets = new byte[sets.length][];
		for (int i = 0; i < bsets.length; i++) {
			bsets[i] = SafeEncoder.encode(sets[i]);
		}
		return zinterstore(SafeEncoder.encode(dstkey), bsets);
	}

	public byte[] zinterstore(final String dstkey,
			final ZParams params, final String... sets) {
		final byte[][] bsets = new byte[sets.length][];
		for (int i = 0; i < bsets.length; i++) {
			bsets[i] = SafeEncoder.encode(sets[i]);
		}
		return zinterstore(SafeEncoder.encode(dstkey), params, bsets);
	}

	public byte[] zlexcount(final String key,
			final String min, final String max) {
		return zlexcount(SafeEncoder.encode(key), SafeEncoder.encode(min),
				SafeEncoder.encode(max));
	}

	public byte[] zrangeByLex(final String key,
			final String min, final String max) {
		return zrangeByLex(SafeEncoder.encode(key), SafeEncoder.encode(min),
				SafeEncoder.encode(max));
	}

	public byte[] zrangeByLex(final String key,
			final String min, final String max, final int offset,
			final int count) {
		return zrangeByLex(SafeEncoder.encode(key), SafeEncoder.encode(min),
				SafeEncoder.encode(max), offset, count);
	}

	public byte[] strlen(final String key) {
		return strlen(SafeEncoder.encode(key));
	}

	public byte[] lpushx(final String key,
			final String... string) {
		return lpushx(SafeEncoder.encode(key), getByteParams(string));
	}

	public byte[] persist(final String key) {
		return persist(SafeEncoder.encode(key));
	}

	public byte[] rpushx(final String key,
			final String... string) {
		return rpushx(SafeEncoder.encode(key), getByteParams(string));
	}

	public byte[] echo(final String string) {
		return echo(SafeEncoder.encode(string));
	}

	public byte[] linsert(final String key,
			final LIST_POSITION where, final String pivot, final String value) {
		return linsert(SafeEncoder.encode(key), where,
				SafeEncoder.encode(pivot), SafeEncoder.encode(value));
	}

	public byte[] brpoplpush(String source,
			String destination, int timeout) {
		return brpoplpush(SafeEncoder.encode(source),
				SafeEncoder.encode(destination), timeout);
	}

	public byte[] setbit(final String key, final long offset,
			final boolean value) {
		return setbit(SafeEncoder.encode(key), offset, value);
	}

	public byte[] setbit(final String key, final long offset,
			final String value) {
		return setbit(SafeEncoder.encode(key), offset,
				SafeEncoder.encode(value));
	}

	public byte[] getbit(String key, long offset) {
		return getbit(SafeEncoder.encode(key), offset);
	}

	public byte[] bitpos(final String key,
			final boolean value, final BitPosParams params) {
		return bitpos(SafeEncoder.encode(key), value, params);
	}

	public byte[] setrange(String key, long offset,
			String value) {
		return setrange(SafeEncoder.encode(key), offset,
				SafeEncoder.encode(value));
	}

	public byte[] getrange(String key, long startOffset,
			long endOffset) {
		return getrange(SafeEncoder.encode(key), startOffset, endOffset);
	}

	public byte[] configSet(String parameter, String value) {
		return configSet(SafeEncoder.encode(parameter),
				SafeEncoder.encode(value));
	}

	public byte[] configGet(String pattern) {
		return configGet(SafeEncoder.encode(pattern));
	}

	private byte[][] getByteParams(String... params) {
		byte[][] p = new byte[params.length][];
		for (int i = 0; i < params.length; i++)
			p[i] = SafeEncoder.encode(params[i]);

		return p;
	}

	public byte[] zadd(String key,
			Map<String, Double> scoreMembers) {

		HashMap<byte[], Double> binaryScoreMembers = new HashMap<byte[], Double>();

		for (Map.Entry<String, Double> entry : scoreMembers.entrySet()) {
			binaryScoreMembers
					.put(SafeEncoder.encode(entry.getKey()), entry.getValue());
		}

		return zaddBinary(SafeEncoder.encode(key), binaryScoreMembers);
	}

	public byte[] objectRefcount(String key) {
		return objectRefcount(SafeEncoder.encode(key));
	}

	public byte[] objectIdletime(String key) {
		return objectIdletime(SafeEncoder.encode(key));
	}

	public byte[] objectEncoding(String key) {
		return objectEncoding(SafeEncoder.encode(key));
	}

	public byte[] bitcount(final String key) {
		return bitcount(SafeEncoder.encode(key));
	}

	public byte[] bitcount(final String key, long start,
			long end) {
		return bitcount(SafeEncoder.encode(key), start, end);
	}

	public byte[] bitop(BitOP op, final String destKey,
			String... srcKeys) {
		return bitop(op, SafeEncoder.encode(destKey), getByteParams(srcKeys));
	}

	public byte[] dump(final String key) {
		return dump(SafeEncoder.encode(key));
	}

	public byte[] restore(final String key, final int ttl,
			final byte[] serializedValue) {
		return restore(SafeEncoder.encode(key), ttl, serializedValue);
	}

	public byte[] pexpire(final String key,
			final long milliseconds) {
		return pexpire(SafeEncoder.encode(key), milliseconds);
	}

	public byte[] pexpireAt(final String key,
			final long millisecondsTimestamp) {
		return pexpireAt(SafeEncoder.encode(key), millisecondsTimestamp);
	}

	public byte[] pttl(final String key) {
		return pttl(SafeEncoder.encode(key));
	}

	public byte[] incrByFloat(final String key,
			final double increment) {
		return incrByFloat(SafeEncoder.encode(key), increment);
	}

	public byte[] psetex(final String key,
			final long milliseconds, final String value) {
		return psetex(SafeEncoder.encode(key), milliseconds,
				SafeEncoder.encode(value));
	}

	public byte[] set(final String key, final String value,
			final String nxxx) {
		return set(SafeEncoder.encode(key), SafeEncoder.encode(value),
				SafeEncoder.encode(nxxx));
	}

	public byte[] set(final String key, final String value,
			final String nxxx, final String expx, final int time) {
		return set(SafeEncoder.encode(key), SafeEncoder.encode(value),
				SafeEncoder.encode(nxxx), SafeEncoder.encode(expx), time);
	}

	public byte[] srandmember(final String key,
			final int count) {
		return srandmember(SafeEncoder.encode(key), count);
	}

	public byte[] clientKill(final String client) {
		return clientKill(SafeEncoder.encode(client));
	}

	public byte[] clientSetname(final String name) {
		return clientSetname(SafeEncoder.encode(name));
	}

	public byte[] hincrByFloat(final String key,
			final String field, double increment) {
		return hincrByFloat(SafeEncoder.encode(key), SafeEncoder.encode(field),
				increment);
	}

}
