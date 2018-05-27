package ibsp.cache.client.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ibsp.cache.client.exception.RedisConnectionException;
import ibsp.cache.client.exception.RedisDataException;

public final class Protocol {
	
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 6379;
	public static final int DEFAULT_SENTINEL_PORT = 26379;
	public static final int DEFAULT_TIMEOUT = 2000;
	
	public static final String CHARSET = "UTF-8";
	
	public static final byte DOLLAR_BYTE = '$';
	public static final byte ASTERISK_BYTE = '*';
	public static final byte PLUS_BYTE = '+';
	public static final byte MINUS_BYTE = '-';
	public static final byte COLON_BYTE = ':';
	
	public static final byte[] BYTES_TRUE = toByteArray(1);
	public static final byte[] BYTES_FALSE = toByteArray(0);
	
	private Protocol() {
		
	}
	
	public static void sendCommand(final RedisOutputStream os, final ProtocolCommand command, final byte[]... args) {
		sendCommand(os, command.getRaw(), args);
	}

	private static void sendCommand(final RedisOutputStream os, final byte[] command, final byte[]... args) {
		try {
			os.write(ASTERISK_BYTE);
			os.writeIntCrLf(args.length + 1);
			os.write(DOLLAR_BYTE);
			os.writeIntCrLf(command.length);
			os.write(command);
			os.writeCrLf();

			for (final byte[] arg : args) {
				os.write(DOLLAR_BYTE);
				os.writeIntCrLf(arg.length);
				os.write(arg);
				os.writeCrLf();
			}
		} catch (IOException e) {
			throw new RedisConnectionException(e);
		}
	}
	
	public static Object read(final RedisInputStream is) {
		return process(is);
	}
	
	private static Object process(final RedisInputStream is) {

		final byte b = is.readByte();
		if (b == PLUS_BYTE) {
			return processStatusCodeReply(is);
		} else if (b == DOLLAR_BYTE) {
			return processBulkReply(is);
		} else if (b == ASTERISK_BYTE) {
			return processMultiBulkReply(is);
		} else if (b == COLON_BYTE) {
			return processInteger(is);
		} else if (b == MINUS_BYTE) {
			processError(is);
			return null;
		} else {
			throw new RedisConnectionException("Unknown reply: " + (char) b);
		}
	}
	
	private static byte[] processStatusCodeReply(final RedisInputStream is) {
		return is.readLineBytes();
	}
	
	private static byte[] processBulkReply(final RedisInputStream is) {
		final int len = is.readIntCrLf();
		if (len == -1) {
			return null;
		}

		final byte[] read = new byte[len];
		int offset = 0;
		while (offset < len) {
			final int size = is.read(read, offset, (len - offset));
			if (size == -1)
				throw new RedisConnectionException("It seems like server has closed the connection.");
			offset += size;
		}

		// read 2 more bytes for the command delimiter
		is.readByte();
		is.readByte();

		return read;
	}	

	private static List<Object> processMultiBulkReply(final RedisInputStream is) {
		final int num = is.readIntCrLf();
		if (num == -1) {
			return null;
		}
		final List<Object> ret = new ArrayList<Object>(num);
		for (int i = 0; i < num; i++) {
			try {
				ret.add(process(is));
			} catch (RedisDataException e) {
				ret.add(e);
			}
		}
		return ret;
	}
	
	private static Long processInteger(final RedisInputStream is) {
		return is.readLongCrLf();
	}
	
	private static void processError(final RedisInputStream is) {
		String message = is.readLine();
		throw new RedisDataException(message);
	}
	
	public static final byte[] toByteArray(final boolean value) {
		return value ? BYTES_TRUE : BYTES_FALSE;
	}

	public static final byte[] toByteArray(final int value) {
		return SafeEncoder.encode(String.valueOf(value));
	}

	public static final byte[] toByteArray(final long value) {
		return SafeEncoder.encode(String.valueOf(value));
	}

	public static final byte[] toByteArray(final double value) {
		return SafeEncoder.encode(String.valueOf(value));
	}

	public static enum Command implements ProtocolCommand {
		PING, SET, GET, QUIT, EXISTS, DEL, TYPE, FLUSHDB, KEYS, RANDOMKEY, RENAME, RENAMENX, RENAMEX, DBSIZE, EXPIRE,
		EXPIREAT, TTL, SELECT, MOVE, FLUSHALL, GETSET, MGET, SETNX, SETEX, MSET, MSETNX, DECRBY, DECR, INCRBY, INCR,
		APPEND, SUBSTR, HSET, HGET, HSETNX, HMSET, HMGET, HINCRBY, HEXISTS, HDEL, HLEN, HKEYS, HVALS, HGETALL, RPUSH,
		LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP, RPOPLPUSH, SADD, SMEMBERS, SREM, SPOP, SMOVE, SCARD,
		SISMEMBER, SINTER, SINTERSTORE, SUNION, SUNIONSTORE, SDIFF, SDIFFSTORE, SRANDMEMBER, ZADD, ZRANGE, ZREM, ZINCRBY,
		ZRANK, ZREVRANK, ZREVRANGE, ZCARD, ZSCORE, MULTI, DISCARD, EXEC, WATCH, UNWATCH, SORT, BLPOP, BRPOP, AUTH,
		ZCOUNT, ZRANGEBYSCORE, ZREVRANGEBYSCORE,
		ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZUNIONSTORE, ZINTERSTORE, ZLEXCOUNT, ZRANGEBYLEX, ZREVRANGEBYLEX, ZREMRANGEBYLEX,
		SAVE, BGSAVE, BGREWRITEAOF, LASTSAVE, SHUTDOWN, INFO, MONITOR, SLAVEOF, CONFIG, STRLEN, SYNC, LPUSHX, PERSIST, RPUSHX,
		ECHO, LINSERT, DEBUG, BRPOPLPUSH, SETBIT, GETBIT, BITPOS, SETRANGE, GETRANGE, SLOWLOG, OBJECT,
		BITCOUNT, BITOP, SENTINEL, DUMP, RESTORE, PEXPIRE, PEXPIREAT, PTTL, INCRBYFLOAT, PSETEX, CLIENT, TIME, MIGRATE,
		HINCRBYFLOAT, WAIT, CLUSTER, ASKING, PFADD, PFCOUNT, PFMERGE;

		private final byte[] raw;

		Command() {
			raw = SafeEncoder.encode(this.name());
		}

		@Override
		public byte[] getRaw() {
			return raw;
		}
	}
	
	public static enum Keyword {
		AGGREGATE, ALPHA, ASC, BY, DESC, GET, LIMIT, MESSAGE, NO, NOSORT, PMESSAGE, PSUBSCRIBE, PUNSUBSCRIBE, OK, ONE, 
		QUEUED, SET, STORE, SUBSCRIBE, UNSUBSCRIBE, WEIGHTS, WITHSCORES, RESETSTAT, RESET, FLUSH, EXISTS, LOAD, KILL, LEN, 
		REFCOUNT, ENCODING, IDLETIME, AND, OR, XOR, NOT, GETNAME, SETNAME, LIST, MATCH, COUNT;
		public final byte[] raw;

		Keyword() {
			raw = SafeEncoder.encode(this.name().toLowerCase());
		}
	}

}
