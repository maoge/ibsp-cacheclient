package ibsp.cache.client.core;

import static ibsp.cache.client.protocol.Protocol.toByteArray;
import static ibsp.cache.client.protocol.Protocol.Command.BITOP;
import static ibsp.cache.client.protocol.Protocol.Command.CLIENT;
import static ibsp.cache.client.protocol.Protocol.Command.DUMP;
import static ibsp.cache.client.protocol.Protocol.Command.HINCRBYFLOAT;
import static ibsp.cache.client.protocol.Protocol.Command.PEXPIRE;
import static ibsp.cache.client.protocol.Protocol.Command.PEXPIREAT;
import static ibsp.cache.client.protocol.Protocol.Command.PING;
import static ibsp.cache.client.protocol.Protocol.Command.PSETEX;
import static ibsp.cache.client.protocol.Protocol.Command.PTTL;
import static ibsp.cache.client.protocol.Protocol.Command.RESTORE;
import static ibsp.cache.client.protocol.Protocol.Command.SRANDMEMBER;
import static ibsp.cache.client.protocol.Protocol.Command.TIME;
import static ibsp.cache.client.protocol.Protocol.Command.WAIT;
import static ibsp.cache.client.protocol.Protocol.Keyword.LIMIT;
import static ibsp.cache.client.protocol.Protocol.Keyword.NO;
import static ibsp.cache.client.protocol.Protocol.Keyword.ONE;
import static ibsp.cache.client.protocol.Protocol.Keyword.STORE;
import static ibsp.cache.client.protocol.Protocol.Keyword.WITHSCORES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import ibsp.cache.client.command.BinaryClient.LIST_POSITION;
import ibsp.cache.client.exception.RedisException;
import ibsp.cache.client.protocol.BitOP;
import ibsp.cache.client.protocol.BitPosParams;
import ibsp.cache.client.protocol.Protocol;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.protocol.SortingParams;
import ibsp.cache.client.protocol.ZParams;
import ibsp.cache.client.protocol.Protocol.Command;
import ibsp.cache.client.protocol.Protocol.Keyword;
import ibsp.cache.client.utils.CONSTS;

public class NBinaryClient extends NJedisConnection {

	private boolean isInMulti;
	private int db;
	private boolean isInWatch;
	private ThreadLocal<Nheader> groupInfo = new ThreadLocal<Nheader>();

	public NBinaryClient(String host, int port, int timeOut, String connectionName, boolean isSync) {
		super(host, port, timeOut, connectionName, isSync);
	}

	public NBinaryClient(String host, int port, int timeOut, boolean isSync) {
		super(host, port, timeOut, isSync);
	}

	public NBinaryClient(String host, int port, boolean isSync) {
		super(host, port, isSync);
	}

	public void setCurrentHeader(Nheader header) {
		groupInfo.set(header);
	}

	protected Nheader getCurrentHeader() {
		return groupInfo.get();
	}

	public void removeCurrentHeader() {
		groupInfo.remove();
	}

	public boolean isInMulti() {
		return isInMulti;
	}

	public boolean isInWatch() {
		return isInWatch;
	}

	private byte[][] joinParameters(byte[] first, byte[][] rest) {
		byte[][] result = new byte[rest.length + 1][];
		result[0] = first;
		System.arraycopy(rest, 0, result, 1, rest.length);
		return result;
	}

	public void setDb(int db) {
		this.db = db;
	}

	public byte[] sendCommand(Command cmd) {
		try {
			return this.connection.call(new ParaEntity(cmd, getCurrentHeader()));
		} catch (InterruptedException e) {
			throw new RedisException("连接异常", e);
		} catch (TimeoutException e) {
			throw new RedisException(e.getMessage(), e);
		} finally {
			removeCurrentHeader();
		}
	}

	public byte[] sendCommand(Command cmd, int timeout) {
		try {
			return this.connection.call(new ParaEntity(cmd, getCurrentHeader()), timeout);
		} catch (InterruptedException e) {
			throw new RedisException("连接异常", e);
		} catch (TimeoutException e) {
			throw new RedisException(e.getMessage(), e);
		} finally {
			removeCurrentHeader();
		}
	}

	public byte[] sendCommand(Command cmd, final byte[]... args) {
		try {
			return this.connection.call(new ParaEntity(cmd, getCurrentHeader(), args));
		} catch (InterruptedException e) {
			throw new RedisException("连接异常", e);
		} catch (TimeoutException e) {
			throw new RedisException(e.getMessage(), e);
		} finally {
			removeCurrentHeader();
		}
	}

	public byte[] sendCommand(Command cmd, int timeout, final byte[]... args) {
		try {
			return this.connection.call(new ParaEntity(cmd, getCurrentHeader(), args), timeout);
		} catch (InterruptedException e) {
			throw new RedisException("连接异常", e);
		} catch (TimeoutException e) {
			throw new RedisException(e.getMessage(), e);
		} finally {
			removeCurrentHeader();
		}
	}

	public byte[] sendCommand(Command cmd, final String... args) {
		final byte[][] bargs = SafeEncoder.encodeMany(args);
		return sendCommand(cmd, bargs);
	}

	public byte[] ping() {
		return sendCommand(PING);
	}

	public byte[] set(final byte[] key, final byte[] value) {
		return sendCommand(Command.SET, key, value);
	}

	public byte[] set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final long time) {
		return sendCommand(Command.SET, key, value, nxxx, expx, toByteArray(time));
	}

	public byte[] get(final byte[] key) {
		return sendCommand(Command.GET, key);
	}

	public byte[] quit() {
		db = 0;
		return sendCommand(Command.QUIT);
	}

	public byte[] exists(final byte[] key) {
		return sendCommand(Command.EXISTS, key);
	}

	public byte[] del(final byte[]... keys) {
		return sendCommand(Command.DEL, keys);
	}

	public byte[] type(final byte[] key) {
		return sendCommand(Command.TYPE, key);
	}

	public byte[] flushDB() {
		return sendCommand(Command.FLUSHDB);
	}

	public byte[] keys(final byte[] pattern) {
		return sendCommand(Command.KEYS, pattern);
	}

	public byte[] randomKey() {
		return sendCommand(Command.RANDOMKEY);
	}

	public byte[] rename(final byte[] oldkey, final byte[] newkey) {
		return sendCommand(Command.RENAME, oldkey, newkey);
	}

	public byte[] renamenx(final byte[] oldkey, final byte[] newkey) {
		return sendCommand(Command.RENAMENX, oldkey, newkey);
	}

	public byte[] dbSize() {
		return sendCommand(Command.DBSIZE);
	}

	public byte[] expire(final byte[] key, final int seconds) {
		return sendCommand(Command.EXPIRE, key, toByteArray(seconds));
	}

	public byte[] expireAt(final byte[] key, final long unixTime) {
		return sendCommand(Command.EXPIREAT, key, toByteArray(unixTime));
	}

	public byte[] ttl(final byte[] key) {
		return sendCommand(Command.TTL, key);
	}

	public byte[] select(final int index) {
		return sendCommand(Command.SELECT, toByteArray(index));
	}

	public byte[] move(final byte[] key, final int dbIndex) {
		return sendCommand(Command.MOVE, key, toByteArray(dbIndex));
	}

	public byte[] flushAll() {
		return sendCommand(Command.FLUSHALL);
	}

	public byte[] getSet(final byte[] key, final byte[] value) {
		return sendCommand(Command.GETSET, key, value);
	}

	public byte[] mget(final byte[]... keys) {
		return sendCommand(Command.MGET, keys);
	}

	public byte[] setnx(final byte[] key, final byte[] value) {
		return sendCommand(Command.SETNX, key, value);
	}

	public byte[] setex(final byte[] key, final int seconds, final byte[] value) {
		return sendCommand(Command.SETEX, key, toByteArray(seconds), value);
	}

	public byte[] mset(final byte[]... keysvalues) {
		return sendCommand(Command.MSET, keysvalues);
	}

	public byte[] msetnx(final byte[]... keysvalues) {
		return sendCommand(Command.MSETNX, keysvalues);
	}

	public byte[] decrBy(final byte[] key, final long integer) {
		return sendCommand(Command.DECRBY, key, toByteArray(integer));
	}

	public byte[] decr(final byte[] key) {
		return sendCommand(Command.DECR, key);
	}

	public byte[] incrBy(final byte[] key, final long integer) {
		return sendCommand(Command.INCRBY, key, toByteArray(integer));
	}

	public byte[] incrByFloat(final byte[] key, final double value) {
		return sendCommand(Command.INCRBYFLOAT, key, toByteArray(value));
	}

	public byte[] incr(final byte[] key) {
		return sendCommand(Command.INCR, key);
	}

	public byte[] append(final byte[] key, final byte[] value) {
		return sendCommand(Command.APPEND, key, value);
	}

	public byte[] substr(final byte[] key, final int start, final int end) {
		return sendCommand(Command.SUBSTR, key, toByteArray(start), toByteArray(end));
	}

	public byte[] hset(final byte[] key, final byte[] field, final byte[] value) {
		return sendCommand(Command.HSET, key, field, value);
	}

	public byte[] hget(final byte[] key, final byte[] field) {
		return sendCommand(Command.HGET, key, field);
	}

	public byte[] hsetnx(final byte[] key, final byte[] field, final byte[] value) {
		return sendCommand(Command.HSETNX, key, field, value);
	}

	public byte[] hmset(final byte[] key, final Map<byte[], byte[]> hash) {
		final List<byte[]> params = new ArrayList<byte[]>();
		params.add(key);

		for (final Entry<byte[], byte[]> entry : hash.entrySet()) {
			params.add(entry.getKey());
			params.add(entry.getValue());
		}
		return sendCommand(Command.HMSET, params.toArray(new byte[params.size()][]));
	}

	public byte[] hmget(final byte[] key, final byte[]... fields) {
		final byte[][] params = new byte[fields.length + 1][];
		params[0] = key;
		System.arraycopy(fields, 0, params, 1, fields.length);
		return sendCommand(Command.HMGET, params);
	}

	public byte[] hincrBy(final byte[] key, final byte[] field, final long value) {
		return sendCommand(Command.HINCRBY, key, field, toByteArray(value));
	}

	public byte[] hexists(final byte[] key, final byte[] field) {
		return sendCommand(Command.HEXISTS, key, field);
	}

	public byte[] hdel(final byte[] key, final byte[]... fields) {
		return sendCommand(Command.HDEL, joinParameters(key, fields));
	}

	public byte[] hlen(final byte[] key) {
		return sendCommand(Command.HLEN, key);
	}

	public byte[] hkeys(final byte[] key) {
		return sendCommand(Command.HKEYS, key);
	}

	public byte[] hvals(final byte[] key) {
		return sendCommand(Command.HVALS, key);
	}

	public byte[] hgetAll(final byte[] key) {
		return sendCommand(Command.HGETALL, key);
	}

	public byte[] rpush(final byte[] key, final byte[]... strings) {
		return sendCommand(Command.RPUSH, joinParameters(key, strings));
	}

	public byte[] lpush(final byte[] key, final byte[]... strings) {
		return sendCommand(Command.LPUSH, joinParameters(key, strings));
	}

	public byte[] llen(final byte[] key) {
		return sendCommand(Command.LLEN, key);
	}

	public byte[] lrange(final byte[] key, final long start, final long end) {
		return sendCommand(Command.LRANGE, key, toByteArray(start), toByteArray(end));
	}

	public byte[] ltrim(final byte[] key, final long start, final long end) {
		return sendCommand(Command.LTRIM, key, toByteArray(start), toByteArray(end));
	}

	public byte[] lindex(final byte[] key, final long index) {
		return sendCommand(Command.LINDEX, key, toByteArray(index));
	}

	public byte[] lset(final byte[] key, final long index, final byte[] value) {
		return sendCommand(Command.LSET, key, toByteArray(index), value);
	}

	public byte[] lrem(final byte[] key, long count, final byte[] value) {
		return sendCommand(Command.LREM, key, toByteArray(count), value);
	}

	public byte[] lpop(final byte[] key) {
		return sendCommand(Command.LPOP, key);
	}

	public byte[] rpop(final byte[] key) {
		return sendCommand(Command.RPOP, key);
	}

	public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
		return sendCommand(Command.RPOPLPUSH, srckey, dstkey);
	}

	public byte[] sadd(final byte[] key, final byte[]... members) {
		return sendCommand(Command.SADD, joinParameters(key, members));
	}

	public byte[] smembers(final byte[] key) {
		return sendCommand(Command.SMEMBERS, key);
	}

	public byte[] srem(final byte[] key, final byte[]... members) {
		return sendCommand(Command.SREM, joinParameters(key, members));
	}

	public byte[] spop(final byte[] key) {
		return sendCommand(Command.SPOP, key);
	}

	public byte[] spop(final byte[] key, final long count) {
		return sendCommand(Command.SPOP, key, toByteArray(count));
	}

	public byte[] smove(final byte[] srckey, final byte[] dstkey, final byte[] member) {
		return sendCommand(Command.SMOVE, srckey, dstkey, member);
	}

	public byte[] scard(final byte[] key) {
		return sendCommand(Command.SCARD, key);
	}

	public byte[] sismember(final byte[] key, final byte[] member) {
		return sendCommand(Command.SISMEMBER, key, member);
	}

	public byte[] sinter(final byte[]... keys) {
		return sendCommand(Command.SINTER, keys);
	}

	public byte[] sinterstore(final byte[] dstkey, final byte[]... keys) {
		final byte[][] params = new byte[keys.length + 1][];
		params[0] = dstkey;
		System.arraycopy(keys, 0, params, 1, keys.length);
		return sendCommand(Command.SINTERSTORE, params);
	}

	public byte[] sunion(final byte[]... keys) {
		return sendCommand(Command.SUNION, keys);
	}

	public byte[] sunionstore(final byte[] dstkey, final byte[]... keys) {
		byte[][] params = new byte[keys.length + 1][];
		params[0] = dstkey;
		System.arraycopy(keys, 0, params, 1, keys.length);
		return sendCommand(Command.SUNIONSTORE, params);
	}

	public byte[] sdiff(final byte[]... keys) {
		return sendCommand(Command.SDIFF, keys);
	}

	public byte[] sdiffstore(final byte[] dstkey, final byte[]... keys) {
		byte[][] params = new byte[keys.length + 1][];
		params[0] = dstkey;
		System.arraycopy(keys, 0, params, 1, keys.length);
		return sendCommand(Command.SDIFFSTORE, params);
	}

	public byte[] srandmember(final byte[] key) {
		return sendCommand(Command.SRANDMEMBER, key);
	}

	public byte[] zadd(final byte[] key, final double score, final byte[] member) {
		return sendCommand(Command.ZADD, key, toByteArray(score), member);
	}

	public byte[] zaddBinary(final byte[] key, final Map<byte[], Double> scoreMembers) {

		ArrayList<byte[]> args = new ArrayList<byte[]>(scoreMembers.size() * 2 + 1);
		args.add(key);

		for (Map.Entry<byte[], Double> entry : scoreMembers.entrySet()) {
			args.add(toByteArray(entry.getValue()));
			args.add(entry.getKey());
		}

		byte[][] argsArray = new byte[args.size()][];
		args.toArray(argsArray);

		return sendCommand(Command.ZADD, argsArray);
	}

	public byte[] zrange(final byte[] key, final long start, final long end) {
		return sendCommand(Command.ZRANGE, key, toByteArray(start), toByteArray(end));
	}

	public byte[] zrem(final byte[] key, final byte[]... members) {
		return sendCommand(Command.ZREM, joinParameters(key, members));
	}

	public byte[] zincrby(final byte[] key, final double score, final byte[] member) {
		return sendCommand(Command.ZINCRBY, key, toByteArray(score), member);
	}

	public byte[] zrank(final byte[] key, final byte[] member) {
		return sendCommand(Command.ZRANK, key, member);
	}

	public byte[] zrevrank(final byte[] key, final byte[] member) {
		return sendCommand(Command.ZREVRANK, key, member);
	}

	public byte[] zrevrange(final byte[] key, final long start, final long end) {
		return sendCommand(Command.ZREVRANGE, key, toByteArray(start), toByteArray(end));
	}

	public byte[] zrangeWithScores(final byte[] key, final long start, final long end) {
		return sendCommand(Command.ZRANGE, key, toByteArray(start), toByteArray(end), WITHSCORES.raw);
	}

	public byte[] zrevrangeWithScores(final byte[] key, final long start, final long end) {
		return sendCommand(Command.ZREVRANGE, key, toByteArray(start), toByteArray(end), WITHSCORES.raw);
	}

	public byte[] zcard(final byte[] key) {
		return sendCommand(Command.ZCARD, key);
	}

	public byte[] zscore(final byte[] key, final byte[] member) {
		return sendCommand(Command.ZSCORE, key, member);
	}

	public byte[] multi() {
		isInMulti = true;
		return sendCommand(Command.MULTI);
	}

	public byte[] discard() {
		isInMulti = false;
		isInWatch = false;
		return sendCommand(Command.DISCARD);
	}

	public byte[] exec() {
		isInMulti = false;
		isInWatch = false;
		return sendCommand(Command.EXEC);
	}

	public byte[] watch(final byte[]... keys) {
		isInWatch = true;
		return sendCommand(Command.WATCH, keys);
	}

	public byte[] unwatch() {
		isInWatch = false;
		return sendCommand(Command.UNWATCH);
	}

	public byte[] sort(final byte[] key) {
		return sendCommand(Command.SORT, key);
	}

	public byte[] sort(final byte[] key, final SortingParams sortingParameters) {
		final List<byte[]> args = new ArrayList<byte[]>();
		args.add(key);
		args.addAll(sortingParameters.getParams());
		return sendCommand(Command.SORT, args.toArray(new byte[args.size()][]));
	}

	public byte[] blpop(final byte[][] args) {
		return sendCommand(Command.BLPOP, args);
	}

	public byte[] blpop(final int timeout, final byte[]... keys) {
		final List<byte[]> args = new ArrayList<byte[]>();
		for (final byte[] arg : keys) {
			args.add(arg);
		}
		args.add(Protocol.toByteArray(timeout));
		return blpop(args.toArray(new byte[args.size()][]));
	}

	public byte[] sort(final byte[] key, final SortingParams sortingParameters, final byte[] dstkey) {
		final List<byte[]> args = new ArrayList<byte[]>();
		args.add(key);
		args.addAll(sortingParameters.getParams());
		args.add(Keyword.STORE.raw);
		args.add(dstkey);
		return sendCommand(Command.SORT, args.toArray(new byte[args.size()][]));
	}

	public byte[] sort(final byte[] key, final byte[] dstkey) {
		return sendCommand(Command.SORT, key, STORE.raw, dstkey);
	}

	public byte[] brpop(final byte[][] args) {
		return sendCommand(Command.BRPOP, args);
	}

	public byte[] brpop(final int timeout, final byte[]... keys) {
		final List<byte[]> args = new ArrayList<byte[]>();
		for (final byte[] arg : keys) {
			args.add(arg);
		}
		args.add(Protocol.toByteArray(timeout));
		return brpop(args.toArray(new byte[args.size()][]));
	}

	public byte[] zcount(final byte[] key, final double min, final double max) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZCOUNT, key, byteArrayMin, byteArrayMax);
	}

	public byte[] zcount(final byte[] key, final byte min[], final byte max[]) {
		return sendCommand(Command.ZCOUNT, key, min, max);
	}

	public byte[] zcount(final byte[] key, final String min, final String max) {
		return sendCommand(Command.ZCOUNT, key, min.getBytes(), max.getBytes());
	}

	public byte[] zrangeByScore(final byte[] key, final double min, final double max) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZRANGEBYSCORE, key, byteArrayMin, byteArrayMax);
	}

	public byte[] zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
		return sendCommand(Command.ZRANGEBYSCORE, key, min, max);
	}

	public byte[] zrangeByScore(final byte[] key, final String min, final String max) {
		return sendCommand(Command.ZRANGEBYSCORE, key, min.getBytes(), max.getBytes());
	}

	public byte[] zrevrangeByScore(final byte[] key, final double max, final double min) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZREVRANGEBYSCORE, key, byteArrayMax, byteArrayMin);
	}

	public byte[] zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
		return sendCommand(Command.ZREVRANGEBYSCORE, key, max, min);
	}

	public byte[] zrevrangeByScore(final byte[] key, final String max, final String min) {
		return sendCommand(Command.ZREVRANGEBYSCORE, key, max.getBytes(), min.getBytes());
	}

	public byte[] zrangeByScore(final byte[] key, final double min, final double max, final int offset, int count) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZRANGEBYSCORE, key, byteArrayMin, byteArrayMax, LIMIT.raw, toByteArray(offset),
				toByteArray(count));
	}

	public byte[] zrangeByScore(final byte[] key, final String min, final String max, final int offset, int count) {

		return sendCommand(Command.ZRANGEBYSCORE, key, min.getBytes(), max.getBytes(), LIMIT.raw, toByteArray(offset),
				toByteArray(count));
	}

	public byte[] zrevrangeByScore(final byte[] key, final double max, final double min, final int offset, int count) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZREVRANGEBYSCORE, key, byteArrayMax, byteArrayMin, LIMIT.raw, toByteArray(offset),
				toByteArray(count));
	}

	public byte[] zrevrangeByScore(final byte[] key, final String max, final String min, final int offset, int count) {

		return sendCommand(Command.ZREVRANGEBYSCORE, key, max.getBytes(), min.getBytes(), LIMIT.raw,
				toByteArray(offset), toByteArray(count));
	}

	public byte[] zrangeByScoreWithScores(final byte[] key, final double min, final double max) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZRANGEBYSCORE, key, byteArrayMin, byteArrayMax, WITHSCORES.raw);
	}

	public byte[] zrangeByScoreWithScores(final byte[] key, final String min, final String max) {

		return sendCommand(Command.ZRANGEBYSCORE, key, min.getBytes(), max.getBytes(), WITHSCORES.raw);
	}

	public byte[] zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZREVRANGEBYSCORE, key, byteArrayMax, byteArrayMin, WITHSCORES.raw);
	}

	public byte[] zrevrangeByScoreWithScores(final byte[] key, final String max, final String min) {
		return sendCommand(Command.ZREVRANGEBYSCORE, key, max.getBytes(), min.getBytes(), WITHSCORES.raw);
	}

	public byte[] zrangeByScoreWithScores(final byte[] key, final double min, final double max, final int offset,
			final int count) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZRANGEBYSCORE, key, byteArrayMin, byteArrayMax, LIMIT.raw, toByteArray(offset),
				toByteArray(count), WITHSCORES.raw);
	}

	public byte[] zrangeByScoreWithScores(final byte[] key, final String min, final String max, final int offset,
			final int count) {
		return sendCommand(Command.ZRANGEBYSCORE, key, min.getBytes(), max.getBytes(), LIMIT.raw, toByteArray(offset),
				toByteArray(count), WITHSCORES.raw);
	}

	public byte[] zrevrangeByScoreWithScores(final byte[] key, final double max, final double min, final int offset,
			final int count) {

		byte byteArrayMin[] = (min == Double.NEGATIVE_INFINITY) ? "-inf".getBytes() : toByteArray(min);
		byte byteArrayMax[] = (max == Double.POSITIVE_INFINITY) ? "+inf".getBytes() : toByteArray(max);

		return sendCommand(Command.ZREVRANGEBYSCORE, key, byteArrayMax, byteArrayMin, LIMIT.raw, toByteArray(offset),
				toByteArray(count), WITHSCORES.raw);
	}

	public byte[] zrevrangeByScoreWithScores(final byte[] key, final String max, final String min, final int offset,
			final int count) {

		return sendCommand(Command.ZREVRANGEBYSCORE, key, max.getBytes(), min.getBytes(), LIMIT.raw,
				toByteArray(offset), toByteArray(count), WITHSCORES.raw);
	}

	public byte[] zrangeByScore(final byte[] key, final byte[] min, final byte[] max, final int offset, int count) {
		return sendCommand(Command.ZRANGEBYSCORE, key, min, max, LIMIT.raw, toByteArray(offset), toByteArray(count));
	}

	public byte[] zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min, final int offset, int count) {
		return sendCommand(Command.ZREVRANGEBYSCORE, key, max, min, LIMIT.raw, toByteArray(offset), toByteArray(count));
	}

	public byte[] zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
		return sendCommand(Command.ZRANGEBYSCORE, key, min, max, WITHSCORES.raw);
	}

	public byte[] zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
		return sendCommand(Command.ZREVRANGEBYSCORE, key, max, min, WITHSCORES.raw);
	}

	public byte[] zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max, final int offset,
			final int count) {
		return sendCommand(Command.ZRANGEBYSCORE, key, min, max, LIMIT.raw, toByteArray(offset), toByteArray(count),
				WITHSCORES.raw);
	}

	public byte[] zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min, final int offset,
			final int count) {
		return sendCommand(Command.ZREVRANGEBYSCORE, key, max, min, LIMIT.raw, toByteArray(offset), toByteArray(count),
				WITHSCORES.raw);
	}

	public byte[] zremrangeByRank(final byte[] key, final long start, final long end) {
		return sendCommand(Command.ZREMRANGEBYRANK, key, toByteArray(start), toByteArray(end));
	}

	public byte[] zremrangeByScore(final byte[] key, final byte[] start, final byte[] end) {
		return sendCommand(Command.ZREMRANGEBYSCORE, key, start, end);
	}

	public byte[] zremrangeByScore(final byte[] key, final String start, final String end) {
		return sendCommand(Command.ZREMRANGEBYSCORE, key, start.getBytes(), end.getBytes());
	}

	public byte[] zunionstore(final byte[] dstkey, final byte[]... sets) {
		final byte[][] params = new byte[sets.length + 2][];
		params[0] = dstkey;
		params[1] = toByteArray(sets.length);
		System.arraycopy(sets, 0, params, 2, sets.length);
		return sendCommand(Command.ZUNIONSTORE, params);
	}

	public byte[] zunionstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
		final List<byte[]> args = new ArrayList<byte[]>();
		args.add(dstkey);
		args.add(Protocol.toByteArray(sets.length));
		for (final byte[] set : sets) {
			args.add(set);
		}
		args.addAll(params.getParams());
		return sendCommand(Command.ZUNIONSTORE, args.toArray(new byte[args.size()][]));
	}

	public byte[] zinterstore(final byte[] dstkey, final byte[]... sets) {
		final byte[][] params = new byte[sets.length + 2][];
		params[0] = dstkey;
		params[1] = Protocol.toByteArray(sets.length);
		System.arraycopy(sets, 0, params, 2, sets.length);
		return sendCommand(Command.ZINTERSTORE, params);
	}

	public byte[] zinterstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
		final List<byte[]> args = new ArrayList<byte[]>();
		args.add(dstkey);
		args.add(Protocol.toByteArray(sets.length));
		for (final byte[] set : sets) {
			args.add(set);
		}
		args.addAll(params.getParams());
		return sendCommand(Command.ZINTERSTORE, args.toArray(new byte[args.size()][]));
	}

	public byte[] zlexcount(final byte[] key, final byte[] min, final byte[] max) {
		return sendCommand(Command.ZLEXCOUNT, key, min, max);
	}

	public byte[] zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
		return sendCommand(Command.ZRANGEBYLEX, key, min, max);
	}

	public byte[] zrangeByLex(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
		return sendCommand(Command.ZRANGEBYLEX, key, min, max, LIMIT.raw, toByteArray(offset), toByteArray(count));
	}

	public byte[] save() {
		return sendCommand(Command.SAVE);
	}

	public byte[] bgsave() {
		return sendCommand(Command.BGSAVE);
	}

	public byte[] bgrewriteaof() {
		return sendCommand(Command.BGREWRITEAOF);
	}

	public byte[] lastsave() {
		return sendCommand(Command.LASTSAVE);
	}

	public byte[] shutdown() {
		return sendCommand(Command.SHUTDOWN);
	}

	public byte[] info() {
		return sendCommand(Command.INFO);
	}

	public byte[] info(final String section) {
		return sendCommand(Command.INFO, section.getBytes(CONSTS.CHARSET));
	}

	public byte[] monitor() {
		return sendCommand(Command.MONITOR);
	}

	public byte[] slaveof(final String host, final int port) {
		return sendCommand(Command.SLAVEOF, host, String.valueOf(port));
	}

	public byte[] slaveofNoOne() {
		return sendCommand(Command.SLAVEOF, NO.raw, ONE.raw);
	}

	public byte[] configGet(final byte[] pattern) {
		return sendCommand(Command.CONFIG, Keyword.GET.raw, pattern);
	}

	public byte[] configSet(final byte[] parameter, final byte[] value) {
		return sendCommand(Command.CONFIG, Keyword.SET.raw, parameter, value);
	}

	public byte[] strlen(final byte[] key) {
		return sendCommand(Command.STRLEN, key);
	}

	public byte[] sync() {
		return sendCommand(Command.SYNC);
	}

	public byte[] lpushx(final byte[] key, final byte[]... string) {
		return sendCommand(Command.LPUSHX, joinParameters(key, string));
	}

	public byte[] persist(final byte[] key) {
		return sendCommand(Command.PERSIST, key);
	}

	public byte[] rpushx(final byte[] key, final byte[]... string) {
		return sendCommand(Command.RPUSHX, joinParameters(key, string));
	}

	public byte[] echo(final byte[] string) {
		return sendCommand(Command.ECHO, string);
	}

	public byte[] linsert(final byte[] key, final LIST_POSITION where, final byte[] pivot, final byte[] value) {
		return sendCommand(Command.LINSERT, key, where.raw, pivot, value);
	}

	public byte[] brpoplpush(final byte[] source, final byte[] destination, final int timeout) {
		return sendCommand(Command.BRPOPLPUSH, source, destination, toByteArray(timeout));
	}

	public byte[] configResetStat() {
		return sendCommand(Command.CONFIG, Keyword.RESETSTAT.name());
	}

	public byte[] setbit(byte[] key, long offset, byte[] value) {
		return sendCommand(Command.SETBIT, key, toByteArray(offset), value);
	}

	public byte[] setbit(byte[] key, long offset, boolean value) {
		return sendCommand(Command.SETBIT, key, toByteArray(offset), toByteArray(value));
	}

	public byte[] getbit(byte[] key, long offset) {
		return sendCommand(Command.GETBIT, key, toByteArray(offset));
	}

	public byte[] bitpos(final byte[] key, final boolean value, final BitPosParams params) {
		final List<byte[]> args = new ArrayList<byte[]>();
		args.add(key);
		args.add(toByteArray(value));
		args.addAll(params.getParams());
		return sendCommand(Command.BITPOS, args.toArray(new byte[args.size()][]));
	}

	public byte[] setrange(byte[] key, long offset, byte[] value) {
		return sendCommand(Command.SETRANGE, key, toByteArray(offset), value);
	}

	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return sendCommand(Command.GETRANGE, key, toByteArray(startOffset), toByteArray(endOffset));
	}

	public int getDB() {
		return db;
	}

	public byte[] auth(final String password) {
		return sendCommand(Command.AUTH, password.getBytes(CONSTS.CHARSET));
	}

	public void close() {
		this.connection.close();
	}

	public byte[] resetState() {
		if (isInWatch())
			return unwatch();
		return null;
	}

	public byte[] objectRefcount(byte[] key) {
		return sendCommand(Command.OBJECT, Keyword.REFCOUNT.raw, key);
	}

	public byte[] objectIdletime(byte[] key) {
		return sendCommand(Command.OBJECT, Keyword.IDLETIME.raw, key);
	}

	public byte[] objectEncoding(byte[] key) {
		return sendCommand(Command.OBJECT, Keyword.ENCODING.raw, key);
	}

	public byte[] bitcount(byte[] key) {
		return sendCommand(Command.BITCOUNT, key);
	}

	public byte[] bitcount(byte[] key, long start, long end) {
		return sendCommand(Command.BITCOUNT, key, toByteArray(start), toByteArray(end));
	}

	public byte[] bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
		Keyword kw = Keyword.AND;
		int len = srcKeys.length;
		switch (op) {
		case AND:
			kw = Keyword.AND;
			break;
		case OR:
			kw = Keyword.OR;
			break;
		case XOR:
			kw = Keyword.XOR;
			break;
		case NOT:
			kw = Keyword.NOT;
			len = Math.min(1, len);
			break;
		}

		byte[][] bargs = new byte[len + 2][];
		bargs[0] = kw.raw;
		bargs[1] = destKey;
		for (int i = 0; i < len; ++i) {
			bargs[i + 2] = srcKeys[i];
		}

		return sendCommand(BITOP, bargs);
	}

	public byte[] dump(final byte[] key) {
		return sendCommand(DUMP, key);
	}

	public byte[] restore(final byte[] key, final int ttl, final byte[] serializedValue) {
		return sendCommand(RESTORE, key, toByteArray(ttl), serializedValue);
	}

	public byte[] pexpire(final String key, final long milliseconds) {
		return sendCommand(PEXPIRE, SafeEncoder.encode(key), toByteArray(milliseconds));
	}

	public byte[] pexpire(final byte[] key, final long milliseconds) {
		return sendCommand(PEXPIRE, key, toByteArray(milliseconds));
	}

	public byte[] pexpireAt(final byte[] key, final long millisecondsTimestamp) {
		return sendCommand(PEXPIREAT, key, toByteArray(millisecondsTimestamp));
	}

	public byte[] pttl(final byte[] key) {
		return sendCommand(PTTL, key);
	}

	public byte[] psetex(final byte[] key, final long milliseconds, final byte[] value) {
		return sendCommand(PSETEX, key, toByteArray(milliseconds), value);
	}

	public byte[] set(final byte[] key, final byte[] value, final byte[] nxxx) {
		return sendCommand(Command.SET, key, value, nxxx);
	}

	public byte[] set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final int time) {
		return sendCommand(Command.SET, key, value, nxxx, expx, toByteArray(time));
	}

	public byte[] srandmember(final byte[] key, final int count) {
		return sendCommand(SRANDMEMBER, key, toByteArray(count));
	}

	public byte[] clientKill(final byte[] client) {
		return sendCommand(CLIENT, Keyword.KILL.raw, client);
	}

	public byte[] clientGetname() {
		return sendCommand(CLIENT, Keyword.GETNAME.raw);
	}

	public byte[] clientList() {
		return sendCommand(CLIENT, Keyword.LIST.raw);
	}

	public byte[] clientSetname(final byte[] name) {
		return sendCommand(CLIENT, Keyword.SETNAME.raw, name);
	}

	public byte[] time() {
		return sendCommand(TIME);
	}

	public byte[] hincrByFloat(final byte[] key, final byte[] field, double increment) {
		return sendCommand(HINCRBYFLOAT, key, field, toByteArray(increment));
	}

	public byte[] waitReplicas(int replicas, long timeout) {
		return sendCommand(WAIT, toByteArray(replicas), toByteArray(timeout));
	}

	public byte[] asking() {
		return sendCommand(Command.ASKING);
	}

}
