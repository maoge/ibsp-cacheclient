package ibsp.cache.client.core;

import static ibsp.cache.client.protocol.Protocol.toByteArray;

import java.io.Closeable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ibsp.cache.client.command.BasicCommands;
import ibsp.cache.client.command.BinaryClient.LIST_POSITION;
import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.command.MultiKeyBinaryCommands;
import ibsp.cache.client.protocol.BitOP;
import ibsp.cache.client.protocol.BitPosParams;
import ibsp.cache.client.protocol.Protocol;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.protocol.SortingParams;
import ibsp.cache.client.protocol.Tuple;
import ibsp.cache.client.protocol.ZParams;
import ibsp.cache.client.utils.JedisByteHashMap;
import ibsp.cache.client.exception.RedisDataException;
import ibsp.cache.client.exception.RedisException;

public class NBinaryJedis implements BasicCommands, BinaryJedisCommands, MultiKeyBinaryCommands, Closeable {

	protected NClient client = null;

	public NBinaryJedis(final String host, final int port, final int timeout, final boolean isSync) {
		this.client = new NClient(host, port, timeout, isSync);
	}

	public NBinaryJedis(final String host, final int port, final int timeout, final String connectionName,
			final boolean isSync) {
		this.client = new NClient(host, port, timeout, connectionName, isSync);
	}

	public void setExceptionList(Map<Exception, String> excepts) {
		this.client.setExceptionList(excepts);
	}

	public void setCurrentHeader(Nheader header) {
		client.setCurrentHeader(header);
	}

	protected byte[] getResultSet(byte[] set) {
		return set;
	}

	protected static byte[][] getParamsWithBinary(List<byte[]> keys, List<byte[]> args) {
		final int keyCount = keys.size();
		final int argCount = args.size();
		byte[][] params = new byte[keyCount + argCount][];

		for (int i = 0; i < keyCount; i++)
			params[i] = keys.get(i);

		for (int i = 0; i < argCount; i++)
			params[keyCount + i] = args.get(i);

		return params;
	}

	public String ping() {
		checkIsInMulti();
		return client.getStatusCodeReply(client.ping());
	}

	/**
	 * Set the string value as value of the key. The string can't be longer than
	 * 1073741824 bytes (1 GB).
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param value
	 * @return Status code reply
	 */
	public String set(final byte[] key, final byte[] value) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.set(key, value)));
	}

	/**
	 * Set the string value as value of the key. The string can't be longer than
	 * 1073741824 bytes (1 GB).
	 *
	 * @param key
	 * @param value
	 * @param nxxx
	 *            NX|XX, NX -- Only set the key if it does not already exist. XX --
	 *            Only set the key if it already exist.
	 * @param expx
	 *            EX|PX, expire time units: EX = seconds; PX = milliseconds
	 * @param time
	 *            expire time in the units of <code>expx</code>
	 * @return Status code reply
	 */
	public String set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final long time) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.set(key, value, nxxx, expx, time)));
	}

	/**
	 * Get the value of the specified key. If the key does not exist the special
	 * value 'nil' is returned. If the value stored at key is not a string an error
	 * is returned because GET can only handle string values.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @return Bulk reply
	 */
	public byte[] get(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.get(key)));
	}

	/**
	 * Ask the server to silently close the connection.
	 */
	public String quit() {
		checkIsInMulti();
		String quitReturn = client.getStatusCodeReply(getResultSet(client.quit()));
		client.close();
		return quitReturn;
	}

	/**
	 * Test if the specified key exists. The command returns "1" if the key exists,
	 * otherwise "0" is returned. Note that even keys set with an empty string as
	 * value will return "1". Time complexity: O(1)
	 *
	 * @param key
	 * @return Integer reply, "1" if the key exists, otherwise "0"
	 */
	public Boolean exists(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.exists(key))) == 1;
	}

	/**
	 * Remove the specified keys. If a given key does not exist no operation is
	 * performed for this key. The command returns the number of keys removed. Time
	 * complexity: O(1)
	 *
	 * @param keys
	 * @return Integer reply, specifically: an integer greater than 0 if one or more
	 *         keys were removed 0 if none of the specified key existed
	 */
	public Long del(final byte[]... keys) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.del(keys)));
	}

	public Long del(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.del(key)));
	}

	/**
	 * Return the type of the value stored at key in form of a string. The type can
	 * be one of "none", "string", "list", "set". "none" is returned if the key does
	 * not exist. Time complexity: O(1)
	 *
	 * @param key
	 * @return Status code reply, specifically: "none" if the key does not exist
	 *         "string" if the key contains a String value "list" if the key
	 *         contains a List value "set" if the key contains a Set value "zset" if
	 *         the key contains a Sorted Set value "hash" if the key contains a Hash
	 *         value
	 */
	public String type(final byte[] key) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.type(key)));
	}

	/**
	 * Returns all the keys matching the glob-style pattern as space separated
	 * strings. For example if you have in the database the keys "foo" and "foobar"
	 * the command "KEYS foo*" will return "foo foobar".
	 * <p/>
	 * Note that while the time complexity for this operation is O(n) the constant
	 * times are pretty low. For example Redis running on an entry level laptop can
	 * scan a 1 million keys database in 40 milliseconds. <b>Still it's better to
	 * consider this one of the slow commands that may ruin the DB performance if
	 * not used with care.</b>
	 * <p/>
	 * In other words this command is intended only for debugging and special
	 * operations like creating a script to change the DB schema. Don't use it in
	 * your normal code. Use Redis Sets in order to group together a subset of
	 * objects.
	 * <p/>
	 * Glob style patterns examples:
	 * <ul>
	 * <li>h?llo will match hello hallo hhllo
	 * <li>h*llo will match hllo heeeello
	 * <li>h[ae]llo will match hello and hallo, but not hillo
	 * </ul>
	 * <p/>
	 * Use \ to escape special chars if you want to match them verbatim.
	 * <p/>
	 * Time complexity: O(n) (with n being the number of keys in the DB, and
	 * assuming keys and pattern of limited length)
	 *
	 * @param pattern
	 * @return Multi bulk reply
	 */
	public Set<byte[]> keys(final byte[] pattern) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.keys(pattern))));
	}

	/**
	 * Return a randomly selected key from the currently selected DB.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @return Singe line reply, specifically the randomly selected key or an empty
	 *         string is the database is empty
	 */
	public byte[] randomBinaryKey() {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.randomKey()));
	}

	/**
	 * Atomically renames the key oldkey to newkey. If the source and destination
	 * name are the same an error is returned. If newkey already exists it is
	 * overwritten.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param oldkey
	 * @param newkey
	 * @return Status code repy
	 */
	public String rename(final byte[] oldkey, final byte[] newkey) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.rename(oldkey, newkey)));
	}

	/**
	 * Rename oldkey into newkey but fails if the destination key newkey already
	 * exists.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param oldkey
	 * @param newkey
	 * @return Integer reply, specifically: 1 if the key was renamed 0 if the target
	 *         key already exist
	 */
	public Long renamenx(final byte[] oldkey, final byte[] newkey) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.renamenx(oldkey, newkey)));
	}

	/**
	 * Return the number of keys in the currently selected database.
	 *
	 * @return Integer reply
	 */
	public Long dbSize() {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.dbSize()));
	}

	/**
	 * Set a timeout on the specified key. After the timeout the key will be
	 * automatically deleted by the server. A key with an associated timeout is said
	 * to be volatile in Redis terminology.
	 * <p/>
	 * Voltile keys are stored on disk like the other keys, the timeout is
	 * persistent too like all the other aspects of the dataset. Saving a dataset
	 * containing expires and stopping the server does not stop the flow of time as
	 * Redis stores on disk the time when the key will no longer be available as
	 * Unix time, and not the remaining seconds.
	 * <p/>
	 * Since Redis 2.1.3 you can update the value of the timeout of a key already
	 * having an expire set. It is also possible to undo the expire at all turning
	 * the key into a normal key using the {@link #persist(byte[]) PERSIST} command.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param seconds
	 * @return Integer reply, specifically: 1: the timeout was set. 0: the timeout
	 *         was not set since the key already has an associated timeout (this may
	 *         happen only in Redis versions &lt; 2.1.3, Redis &gt;= 2.1.3 will
	 *         happily update the timeout), or the key does not exist.
	 * @see <a href="http://redis.io/commands/expire">Expire Command</a>
	 */
	public Long expire(final byte[] key, final int seconds) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.expire(key, seconds)));
	}

	/**
	 * EXPIREAT works exctly like {@link #expire(byte[], int) EXPIRE} but instead to
	 * get the number of seconds representing the Time To Live of the key as a
	 * second argument (that is a relative way of specifing the TTL), it takes an
	 * absolute one in the form of a UNIX timestamp (Number of seconds elapsed since
	 * 1 Gen 1970).
	 * <p/>
	 * EXPIREAT was introduced in order to implement the Append Only File
	 * persistence mode so that EXPIRE commands are automatically translated into
	 * EXPIREAT commands for the append only file. Of course EXPIREAT can also used
	 * by programmers that need a way to simply specify that a given key should
	 * expire at a given time in the future.
	 * <p/>
	 * Since Redis 2.1.3 you can update the value of the timeout of a key already
	 * having an expire set. It is also possible to undo the expire at all turning
	 * the key into a normal key using the {@link #persist(byte[]) PERSIST} command.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param unixTime
	 * @return Integer reply, specifically: 1: the timeout was set. 0: the timeout
	 *         was not set since the key already has an associated timeout (this may
	 *         happen only in Redis versions &lt; 2.1.3, Redis &gt;= 2.1.3 will
	 *         happily update the timeout), or the key does not exist.
	 * @see <a href="http://redis.io/commands/expire">Expire Command</a>
	 */
	public Long expireAt(final byte[] key, final long unixTime) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.expireAt(key, unixTime)));
	}

	/**
	 * The TTL command returns the remaining time to live in seconds of a key that
	 * has an {@link #expire(byte[], int) EXPIRE} set. This introspection capability
	 * allows a Redis client to check how many seconds a given key will continue to
	 * be part of the dataset.
	 *
	 * @param key
	 * @return Integer reply, returns the remaining time to live in seconds of a key
	 *         that has an EXPIRE. If the Key does not exists or does not have an
	 *         associated expire, -1 is returned.
	 */
	public Long ttl(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.ttl(key)));
	}

	/**
	 * Select the DB with having the specified zero-based numeric index. For default
	 * every new client connection is automatically selected to DB 0.
	 *
	 * @param index
	 * @return Status code reply
	 */
	public String select(final int index) {
		checkIsInMulti();
		String statusCodeReply = client.getStatusCodeReply(getResultSet(client.select(index)));
		client.setDb(index);

		return statusCodeReply;
	}

	/**
	 * Move the specified key from the currently selected DB to the specified
	 * destination DB. Note that this command returns 1 only if the key was
	 * successfully moved, and 0 if the target key was already there or if the
	 * source key was not found at all, so it is possible to use MOVE as a locking
	 * primitive.
	 *
	 * @param key
	 * @param dbIndex
	 * @return Integer reply, specifically: 1 if the key was moved 0 if the key was
	 *         not moved because already present on the target DB or was not found
	 *         in the current DB.
	 */
	public Long move(final byte[] key, final int dbIndex) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.move(key, dbIndex)));
	}

	/**
	 * GETSET is an atomic set this value and return the old value command. Set key
	 * to the string value and return the old value stored at key. The string can't
	 * be longer than 1073741824 bytes (1 GB).
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param value
	 * @return Bulk reply
	 */
	public byte[] getSet(final byte[] key, final byte[] value) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.getSet(key, value)));
	}

	/**
	 * Get the values of all the specified keys. If one or more keys dont exist or
	 * is not of type String, a 'nil' value is returned instead of the value of the
	 * specified key, but the operation never fails.
	 * <p/>
	 * Time complexity: O(1) for every key
	 *
	 * @param keys
	 * @return Multi bulk reply
	 */
	public List<byte[]> mget(final byte[]... keys) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.mget(keys)));
	}

	/**
	 * SETNX works exactly like {@link #set(byte[], byte[]) SET} with the only
	 * difference that if the key already exists no operation is performed. SETNX
	 * actually means "SET if Not eXists".
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param value
	 * @return Integer reply, specifically: 1 if the key was set 0 if the key was
	 *         not set
	 */
	public Long setnx(final byte[] key, final byte[] value) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.setnx(key, value)));
	}

	/**
	 * The command is exactly equivalent to the following group of commands:
	 * {@link #set(byte[], byte[]) SET} + {@link #expire(byte[], int) EXPIRE}. The
	 * operation is atomic.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param seconds
	 * @param value
	 * @return Status code reply
	 */
	public String setex(final byte[] key, final int seconds, final byte[] value) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.setex(key, seconds, value)));
	}

	/**
	 * Set the the respective keys to the respective values. MSET will replace old
	 * values with new values, while {@link #msetnx(byte[]...) MSETNX} will not
	 * perform any operation at all even if just a single key already exists.
	 * <p/>
	 * Because of this semantic MSETNX can be used in order to set different keys
	 * representing different fields of an unique logic object in a way that ensures
	 * that either all the fields or none at all are set.
	 * <p/>
	 * Both MSET and MSETNX are atomic operations. This means that for instance if
	 * the keys A and B are modified, another client talking to Redis can either see
	 * the changes to both A and B at once, or no modification at all.
	 *
	 * @param keysvalues
	 * @return Status code reply Basically +OK as MSET can't fail
	 * @see #msetnx(byte[]...)
	 */
	public String mset(final byte[]... keysvalues) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.mset(keysvalues)));
	}

	/**
	 * Set the the respective keys to the respective values. {@link #mset(byte[]...)
	 * MSET} will replace old values with new values, while MSETNX will not perform
	 * any operation at all even if just a single key already exists.
	 * <p/>
	 * Because of this semantic MSETNX can be used in order to set different keys
	 * representing different fields of an unique logic object in a way that ensures
	 * that either all the fields or none at all are set.
	 * <p/>
	 * Both MSET and MSETNX are atomic operations. This means that for instance if
	 * the keys A and B are modified, another client talking to Redis can either see
	 * the changes to both A and B at once, or no modification at all.
	 *
	 * @param keysvalues
	 * @return Integer reply, specifically: 1 if the all the keys were set 0 if no
	 *         key was set (at least one key already existed)
	 * @see #mset(byte[]...)
	 */
	public Long msetnx(final byte[]... keysvalues) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.msetnx(keysvalues)));
	}

	/**
	 * DECRBY work just like {@link #decr(byte[]) INCR} but instead to decrement by
	 * 1 the decrement is integer.
	 * <p/>
	 * INCR commands are limited to 64 bit signed integers.
	 * <p/>
	 * Note: this is actually a string operation, that is, in Redis there are not
	 * "integer" types. Simply the string stored at the key is parsed as a base 10
	 * 64 bit signed integer, incremented, and then converted back as a string.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @param integer
	 * @return Integer reply, this commands will reply with the new value of key
	 *         after the increment.
	 * @see #incr(byte[])
	 * @see #decr(byte[])
	 * @see #incrBy(byte[], long)
	 */
	public Long decrBy(final byte[] key, final long integer) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.decrBy(key, integer)));
	}

	/**
	 * Decrement the number stored at key by one. If the key does not exist or
	 * contains a value of a wrong type, set the key to the value of "0" before to
	 * perform the decrement operation.
	 * <p/>
	 * INCR commands are limited to 64 bit signed integers.
	 * <p/>
	 * Note: this is actually a string operation, that is, in Redis there are not
	 * "integer" types. Simply the string stored at the key is parsed as a base 10
	 * 64 bit signed integer, incremented, and then converted back as a string.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @return Integer reply, this commands will reply with the new value of key
	 *         after the increment.
	 * @see #incr(byte[])
	 * @see #incrBy(byte[], long)
	 * @see #decrBy(byte[], long)
	 */
	public Long decr(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.decr(key)));
	}

	public Long incrBy(final byte[] key, final long integer) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.incrBy(key, integer)));
	}

	public Double incrByFloat(final byte[] key, final double integer) {
		checkIsInMulti();
		String dval = client.getBulkReply(getResultSet(client.incrByFloat(key, integer)));
		return (dval != null ? new Double(dval) : null);
	}

	public Long incr(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.incr(key)));
	}

	public Long append(final byte[] key, final byte[] value) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.append(key, value)));
	}

	public byte[] substr(final byte[] key, final int start, final int end) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.substr(key, start, end)));
	}

	public Long hset(final byte[] key, final byte[] field, final byte[] value) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.hset(key, field, value)));
	}

	public byte[] hget(final byte[] key, final byte[] field) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.hget(key, field)));
	}

	public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.hsetnx(key, field, value)));
	}

	public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.hmset(key, hash)));
	}

	public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.hmget(key, fields)));
	}

	public Long hincrBy(final byte[] key, final byte[] field, final long value) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.hincrBy(key, field, value)));
	}

	public Double hincrByFloat(final byte[] key, final byte[] field, final double value) {
		checkIsInMulti();
		final String dval = client.getBulkReply(getResultSet(client.hincrByFloat(key, field, value)));
		return (dval != null ? new Double(dval) : null);
	}

	public Boolean hexists(final byte[] key, final byte[] field) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.hexists(key, field))) == 1;
	}

	public Long hdel(final byte[] key, final byte[]... fields) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.hdel(key, fields)));
	}

	public Long hlen(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.hlen(key)));
	}

	public Set<byte[]> hkeys(final byte[] key) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.hkeys(key))));
	}

	public List<byte[]> hvals(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.hvals(key)));
	}

	public Map<byte[], byte[]> hgetAll(final byte[] key) {
		checkIsInMulti();
		final List<byte[]> flatHash = client.getBinaryMultiBulkReply(getResultSet(client.hgetAll(key)));
		final Map<byte[], byte[]> hash = new JedisByteHashMap();
		final Iterator<byte[]> iterator = flatHash.iterator();
		while (iterator.hasNext()) {
			hash.put(iterator.next(), iterator.next());
		}

		return hash;
	}

	public Long rpush(final byte[] key, final byte[]... strings) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.rpush(key, strings)));
	}

	public Long lpush(final byte[] key, final byte[]... strings) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.lpush(key, strings)));
	}

	public Long llen(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.llen(key)));
	}

	public List<byte[]> lrange(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.lrange(key, start, end)));
	}

	public String ltrim(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.ltrim(key, start, end)));
	}

	public byte[] lindex(final byte[] key, final long index) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.lindex(key, index)));
	}

	public String lset(final byte[] key, final long index, final byte[] value) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.lset(key, index, value)));
	}

	public Long lrem(final byte[] key, final long count, final byte[] value) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.lrem(key, count, value)));
	}

	public byte[] lpop(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.lpop(key)));
	}

	public byte[] rpop(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.rpop(key)));
	}

	public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.rpoplpush(srckey, dstkey)));
	}

	public Long sadd(final byte[] key, final byte[]... members) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sadd(key, members)));
	}

	public Set<byte[]> smembers(final byte[] key) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.smembers(key))));
	}

	public Long srem(final byte[] key, final byte[]... member) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.srem(key, member)));
	}

	/**
	 * Remove a random element from a Set returning it as return value. If the Set
	 * is empty or the key does not exist, a nil object is returned.
	 * <p/>
	 * The {@link #srandmember(byte[])} command does a similar work but the returned
	 * element is not removed from the Set.
	 * <p/>
	 * Time complexity O(1)
	 *
	 * @param key
	 * @return Bulk reply
	 */
	public byte[] spop(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.spop(key)));
	}

	public Set<byte[]> spop(final byte[] key, final long count) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.spop(key, count))));
	}

	/**
	 * Move the specified member from the set at srckey to the set at dstkey. This
	 * operation is atomic, in every given moment the element will appear to be in
	 * the source or destination set for accessing clients.
	 * <p/>
	 * If the source set does not exist or does not contain the specified element no
	 * operation is performed and zero is returned, otherwise the element is removed
	 * from the source set and added to the destination set. On success one is
	 * returned, even if the element was already present in the destination set.
	 * <p/>
	 * An error is raised if the source or destination keys contain a non Set value.
	 * <p/>
	 * Time complexity O(1)
	 *
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return Integer reply, specifically: 1 if the element was moved 0 if the
	 *         element was not found on the first set and no operation was performed
	 */
	public Long smove(final byte[] srckey, final byte[] dstkey, final byte[] member) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.smove(srckey, dstkey, member)));
	}

	/**
	 * Return the set cardinality (number of elements). If the key does not exist 0
	 * is returned, like for empty sets.
	 *
	 * @param key
	 * @return Integer reply, specifically: the cardinality (number of elements) of
	 *         the set as an integer.
	 */
	public Long scard(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.scard(key)));
	}

	/**
	 * Return 1 if member is a member of the set stored at key, otherwise 0 is
	 * returned.
	 * <p/>
	 * Time complexity O(1)
	 *
	 * @param key
	 * @param member
	 * @return Integer reply, specifically: 1 if the element is a member of the set
	 *         0 if the element is not a member of the set OR if the key does not
	 *         exist
	 */
	public Boolean sismember(final byte[] key, final byte[] member) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sismember(key, member))) == 1;
	}

	/**
	 * Return the members of a set resulting from the intersection of all the sets
	 * hold at the specified keys. Like in {@link #lrange(byte[], long, long)}
	 * LRANGE} the result is sent to the client as a multi-bulk reply (see the
	 * protocol specification for more information). If just a single key is
	 * specified, then this command produces the same result as
	 * {@link #smembers(byte[]) SMEMBERS}. Actually SMEMBERS is just syntax sugar
	 * for SINTER.
	 * <p/>
	 * Non existing keys are considered like empty sets, so if one of the keys is
	 * missing an empty set is returned (since the intersection with an empty set
	 * always is an empty set).
	 * <p/>
	 * Time complexity O(N*M) worst case where N is the cardinality of the smallest
	 * set and M the number of sets
	 *
	 * @param keys
	 * @return Multi bulk reply, specifically the list of common elements.
	 */
	public Set<byte[]> sinter(final byte[]... keys) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.sinter(keys))));
	}

	/**
	 * This commnad works exactly like {@link #sinter(byte[]...) SINTER} but instead
	 * of being returned the resulting set is sotred as dstkey.
	 * <p/>
	 * Time complexity O(N*M) worst case where N is the cardinality of the smallest
	 * set and M the number of sets
	 *
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sinterstore(dstkey, keys)));
	}

	/**
	 * Return the members of a set resulting from the union of all the sets hold at
	 * the specified keys. Like in {@link #lrange(byte[], long, long)} LRANGE} the
	 * result is sent to the client as a multi-bulk reply (see the protocol
	 * specification for more information). If just a single key is specified, then
	 * this command produces the same result as {@link #smembers(byte[]) SMEMBERS}.
	 * <p/>
	 * Non existing keys are considered like empty sets.
	 * <p/>
	 * Time complexity O(N) where N is the total number of elements in all the
	 * provided sets
	 *
	 * @param keys
	 * @return Multi bulk reply, specifically the list of common elements.
	 */
	public Set<byte[]> sunion(final byte[]... keys) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.sunion(keys))));
	}

	/**
	 * This command works exactly like {@link #sunion(byte[]...) SUNION} but instead
	 * of being returned the resulting set is stored as dstkey. Any existing value
	 * in dstkey will be over-written.
	 * <p/>
	 * Time complexity O(N) where N is the total number of elements in all the
	 * provided sets
	 *
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sunionstore(dstkey, keys)));
	}

	/**
	 * Return the difference between the Set stored at key1 and all the Sets key2,
	 * ..., keyN
	 * <p/>
	 * <b>Example:</b>
	 * <p/>
	 * 
	 * <pre>
	 * key1 = [x, a, b, c]
	 * key2 = [c]
	 * key3 = [a, d]
	 * SDIFF key1,key2,key3 =&gt; [x, b]
	 * </pre>
	 * <p/>
	 * Non existing keys are considered like empty sets.
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(N) with N being the total number of elements of all the sets
	 *
	 * @param keys
	 * @return Return the members of a set resulting from the difference between the
	 *         first set provided and all the successive sets.
	 */
	public Set<byte[]> sdiff(final byte[]... keys) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.sdiff(keys))));
	}

	/**
	 * This command works exactly like {@link #sdiff(byte[]...) SDIFF} but instead
	 * of being returned the resulting set is stored in dstkey.
	 *
	 * @param dstkey
	 * @param keys
	 * @return Status code reply
	 */
	public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sdiffstore(dstkey, keys)));
	}

	/**
	 * Return a random element from a Set, without removing the element. If the Set
	 * is empty or the key does not exist, a nil object is returned.
	 * <p/>
	 * The SPOP command does a similar work but the returned element is popped
	 * (removed) from the Set.
	 * <p/>
	 * Time complexity O(1)
	 *
	 * @param key
	 * @return Bulk reply
	 */
	public byte[] srandmember(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.srandmember(key)));
	}

	public List<byte[]> srandmember(final byte[] key, final int count) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.srandmember(key, count)));
	}

	/**
	 * Add the specified member having the specifeid score to the sorted set stored
	 * at key. If member is already a member of the sorted set the score is updated,
	 * and the element reinserted in the right position to ensure sorting. If key
	 * does not exist a new sorted set with the specified member as sole member is
	 * crated. If the key exists but does not hold a sorted set value an error is
	 * returned.
	 * <p/>
	 * The score value can be the string representation of a double precision
	 * floating point number.
	 * <p/>
	 * Time complexity O(log(N)) with N being the number of elements in the sorted
	 * set
	 *
	 * @param key
	 * @param score
	 * @param member
	 * @return Integer reply, specifically: 1 if the new element was added 0 if the
	 *         element was already a member of the sorted set and the score was
	 *         updated
	 */
	public Long zadd(final byte[] key, final double score, final byte[] member) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zadd(key, score, member)));
	}

	public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zaddBinary(key, scoreMembers)));
	}

	public Set<byte[]> zrange(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrange(key, start, end))));
	}

	/**
	 * Remove the specified member from the sorted set value stored at key. If
	 * member was not a member of the set no operation is performed. If key does not
	 * not hold a set value an error is returned.
	 * <p/>
	 * Time complexity O(log(N)) with N being the number of elements in the sorted
	 * set
	 *
	 * @param key
	 * @param members
	 * @return Integer reply, specifically: 1 if the new element was removed 0 if
	 *         the new element was not a member of the set
	 */
	public Long zrem(final byte[] key, final byte[]... members) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zrem(key, members)));
	}

	/**
	 * If member already exists in the sorted set adds the increment to its score
	 * and updates the position of the element in the sorted set accordingly. If
	 * member does not already exist in the sorted set it is added with increment as
	 * score (that is, like if the previous score was virtually zero). If key does
	 * not exist a new sorted set with the specified member as sole member is
	 * crated. If the key exists but does not hold a sorted set value an error is
	 * returned.
	 * <p/>
	 * The score value can be the string representation of a double precision
	 * floating point number. It's possible to provide a negative value to perform a
	 * decrement.
	 * <p/>
	 * For an introduction to sorted sets check the Introduction to Redis data types
	 * page.
	 * <p/>
	 * Time complexity O(log(N)) with N being the number of elements in the sorted
	 * set
	 *
	 * @param key
	 * @param score
	 * @param member
	 * @return The new score
	 */
	public Double zincrby(final byte[] key, final double score, final byte[] member) {
		checkIsInMulti();
		String newscore = client.getBulkReply(getResultSet(client.zincrby(key, score, member)));
		return Double.valueOf(newscore);
	}

	/**
	 * Return the rank (or index) or member in the sorted set at key, with scores
	 * being ordered from low to high.
	 * <p/>
	 * When the given member does not exist in the sorted set, the special value
	 * 'nil' is returned. The returned rank (or index) of the member is 0-based for
	 * both commands.
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))
	 *
	 * @param key
	 * @param member
	 * @return Integer reply or a nil bulk reply, specifically: the rank of the
	 *         element as an integer reply if the element exists. A nil bulk reply
	 *         if there is no such element.
	 * @see #zrevrank(byte[], byte[])
	 */
	public Long zrank(final byte[] key, final byte[] member) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zrank(key, member)));
	}

	/**
	 * Return the rank (or index) or member in the sorted set at key, with scores
	 * being ordered from high to low.
	 * <p/>
	 * When the given member does not exist in the sorted set, the special value
	 * 'nil' is returned. The returned rank (or index) of the member is 0-based for
	 * both commands.
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))
	 *
	 * @param key
	 * @param member
	 * @return Integer reply or a nil bulk reply, specifically: the rank of the
	 *         element as an integer reply if the element exists. A nil bulk reply
	 *         if there is no such element.
	 * @see #zrank(byte[], byte[])
	 */
	public Long zrevrank(final byte[] key, final byte[] member) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zrevrank(key, member)));
	}

	public Set<byte[]> zrevrange(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrevrange(key, start, end))));
	}

	public Set<Tuple> zrangeWithScores(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return getBinaryTupledSet(getResultSet(client.zrangeWithScores(key, start, end)));
	}

	public Set<Tuple> zrevrangeWithScores(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return getBinaryTupledSet(getResultSet(client.zrevrangeWithScores(key, start, end)));
	}

	/**
	 * Return the sorted set cardinality (number of elements). If the key does not
	 * exist 0 is returned, like for empty sorted sets.
	 * <p/>
	 * Time complexity O(1)
	 *
	 * @param key
	 * @return the cardinality (number of elements) of the set as an integer.
	 */
	public Long zcard(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zcard(key)));
	}

	/**
	 * Return the score of the specified element of the sorted set at key. If the
	 * specified element does not exist in the sorted set, or the key does not exist
	 * at all, a special 'nil' value is returned.
	 * <p/>
	 * <b>Time complexity:</b> O(1)
	 *
	 * @param key
	 * @param member
	 * @return the score
	 */
	public Double zscore(final byte[] key, final byte[] member) {
		checkIsInMulti();
		final String score = client.getBulkReply(getResultSet(client.zscore(key, member)));
		return (score != null ? new Double(score) : null);
	}

	protected void checkIsInMulti() {
		if (client.isInMulti()) {
			throw new RedisDataException("Cannot use Jedis when in Multi. Please use JedisTransaction instead.");
		}
	}

	public String watch(final byte[]... keys) {
		return client.getStatusCodeReply(getResultSet(client.watch(keys)));
	}

	public String unwatch() {
		return client.getStatusCodeReply(getResultSet(client.unwatch()));
	}

	@Override
	public void close() {
		client.close();
	}

	public boolean reconnect() {
		return client.connection.reconnection();
	}

	public void release() {
		client.close();
	}

	public void check() throws Exception {
		client.connection.check();
	}

	/**
	 * Sort a Set or a List.
	 * <p/>
	 * Sort the elements contained in the List, Set, or Sorted Set value at key. By
	 * default sorting is numeric with elements being compared as double precision
	 * floating point numbers. This is the simplest form of SORT.
	 *
	 * @param key
	 * @return Assuming the Set/List at key contains a list of numbers, the return
	 *         value will be the list of numbers ordered from the smallest to the
	 *         biggest number.
	 * @see #sort(byte[], byte[])
	 * @see #sort(byte[], SortingParams)
	 * @see #sort(byte[], SortingParams, byte[])
	 */
	public List<byte[]> sort(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.sort(key)));
	}

	/**
	 * Sort a Set or a List accordingly to the specified parameters.
	 * <p/>
	 * <b>examples:</b>
	 * <p/>
	 * Given are the following sets and key/values:
	 * <p/>
	 * 
	 * <pre>
	 * x = [1, 2, 3]
	 * y = [a, b, c]
	 *
	 * k1 = z
	 * k2 = y
	 * k3 = x
	 *
	 * w1 = 9
	 * w2 = 8
	 * w3 = 7
	 * </pre>
	 * <p/>
	 * Sort Order:
	 * <p/>
	 * 
	 * <pre>
	 * sort(x) or sort(x, sp.asc())
	 * -&gt; [1, 2, 3]
	 *
	 * sort(x, sp.desc())
	 * -&gt; [3, 2, 1]
	 *
	 * sort(y)
	 * -&gt; [c, a, b]
	 *
	 * sort(y, sp.alpha())
	 * -&gt; [a, b, c]
	 *
	 * sort(y, sp.alpha().desc())
	 * -&gt; [c, a, b]
	 * </pre>
	 * <p/>
	 * Limit (e.g. for Pagination):
	 * <p/>
	 * 
	 * <pre>
	 * sort(x, sp.limit(0, 2))
	 * -&gt; [1, 2]
	 *
	 * sort(y, sp.alpha().desc().limit(1, 2))
	 * -&gt; [b, a]
	 * </pre>
	 * <p/>
	 * Sorting by external keys:
	 * <p/>
	 * 
	 * <pre>
	 * sort(x, sb.by(w*))
	 * -&gt; [3, 2, 1]
	 *
	 * sort(x, sb.by(w*).desc())
	 * -&gt; [1, 2, 3]
	 * </pre>
	 * <p/>
	 * Getting external keys:
	 * <p/>
	 * 
	 * <pre>
	 * sort(x, sp.by(w*).get(k*))
	 * -&gt; [x, y, z]
	 *
	 * sort(x, sp.by(w*).get(#).get(k*))
	 * -&gt; [3, x, 2, y, 1, z]
	 * </pre>
	 *
	 * @param key
	 * @param sortingParameters
	 * @return a list of sorted elements.
	 * @see #sort(byte[])
	 * @see #sort(byte[], SortingParams, byte[])
	 */
	// public List<byte[]> sort(final byte[] key,
	// final SortingParams sortingParameters) {
	// checkIsInMulti();
	// return client.getBinaryMultiBulkReply(getResultSet(client.sort(key,
	// sortingParameters)));
	// }

	/**
	 * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this commands
	 * as blocking versions of LPOP and RPOP able to block if the specified keys
	 * don't exist or contain empty lists.
	 * <p/>
	 * The following is a description of the exact semantic. We describe BLPOP but
	 * the two commands are identical, the only difference is that BLPOP pops the
	 * element from the left (head) of the list, and BRPOP pops from the right
	 * (tail).
	 * <p/>
	 * <b>Non blocking behavior</b>
	 * <p/>
	 * When BLPOP is called, if at least one of the specified keys contain a non
	 * empty list, an element is popped from the head of the list and returned to
	 * the caller together with the name of the key (BLPOP returns a two elements
	 * array, the first element is the key, the second the popped value).
	 * <p/>
	 * Keys are scanned from left to right, so for instance if you issue BLPOP list1
	 * list2 list3 0 against a dataset where list1 does not exist but list2 and
	 * list3 contain non empty lists, BLPOP guarantees to return an element from the
	 * list stored at list2 (since it is the first non empty list starting from the
	 * left).
	 * <p/>
	 * <b>Blocking behavior</b>
	 * <p/>
	 * If none of the specified keys exist or contain non empty lists, BLPOP blocks
	 * until some other client performs a LPUSH or an RPUSH operation against one of
	 * the lists.
	 * <p/>
	 * Once new data is present on one of the lists, the client finally returns with
	 * the name of the key unblocking it and the popped value.
	 * <p/>
	 * When blocking, if a non-zero timeout is specified, the client will unblock
	 * returning a nil special value if the specified amount of seconds passed
	 * without a push operation against at least one of the specified keys.
	 * <p/>
	 * The timeout argument is interpreted as an integer value. A timeout of zero
	 * means instead to block forever.
	 * <p/>
	 * <b>Multiple clients blocking for the same keys</b>
	 * <p/>
	 * Multiple clients can block for the same key. They are put into a queue, so
	 * the first to be served will be the one that started to wait earlier, in a
	 * first-blpopping first-served fashion.
	 * <p/>
	 * <b>blocking POP inside a MULTI/EXEC transaction</b>
	 * <p/>
	 * BLPOP and BRPOP can be used with pipelining (sending multiple commands and
	 * reading the replies in batch), but it does not make sense to use BLPOP or
	 * BRPOP inside a MULTI/EXEC block (a Redis transaction).
	 * <p/>
	 * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to return a
	 * multi-bulk nil reply, exactly what happens when the timeout is reached. If
	 * you like science fiction, think at it like if inside MULTI/EXEC the time will
	 * flow at infinite speed :)
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param timeout
	 * @param keys
	 * @return BLPOP returns a two-elements array via a multi bulk reply in order to
	 *         return both the unblocking key and the popped value.
	 *         <p/>
	 *         When a non-zero timeout is specified, and the BLPOP operation timed
	 *         out, the return value is a nil multi bulk reply. Most client values
	 *         will return false or nil accordingly to the programming language
	 *         used.
	 * @see #brpop(int, byte[]...)
	 */
	public List<byte[]> blpop(final int timeout, final byte[]... keys) {
		return blpop(getArgsAddTimeout(timeout, keys));
	}

	private byte[][] getArgsAddTimeout(int timeout, byte[][] keys) {
		int size = keys.length;
		final byte[][] args = new byte[size + 1][];
		for (int at = 0; at != size; ++at) {
			args[at] = keys[at];
		}
		args[size] = Protocol.toByteArray(timeout);
		return args;
	}

	/**
	 * Sort a Set or a List accordingly to the specified parameters and store the
	 * result at dstkey.
	 *
	 * @param key
	 * @param sortingParameters
	 * @param dstkey
	 * @return The number of elements of the list at dstkey.
	 * @see #sort(byte[], SortingParams)
	 * @see #sort(byte[])
	 * @see #sort(byte[], byte[])
	 */
	public Long sort(final byte[] key, final SortingParams sortingParameters, final byte[] dstkey) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sort(key, sortingParameters, dstkey)));
	}

	/**
	 * Sort a Set or a List and Store the Result at dstkey.
	 * <p/>
	 * Sort the elements contained in the List, Set, or Sorted Set value at key and
	 * store the result at dstkey. By default sorting is numeric with elements being
	 * compared as double precision floating point numbers. This is the simplest
	 * form of SORT.
	 *
	 * @param key
	 * @param dstkey
	 * @return The number of elements of the list at dstkey.
	 * @see #sort(byte[])
	 * @see #sort(byte[], SortingParams)
	 * @see #sort(byte[], SortingParams, byte[])
	 */
	public Long sort(final byte[] key, final byte[] dstkey) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.sort(key, dstkey)));
	}

	/**
	 * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this commands
	 * as blocking versions of LPOP and RPOP able to block if the specified keys
	 * don't exist or contain empty lists.
	 * <p/>
	 * The following is a description of the exact semantic. We describe BLPOP but
	 * the two commands are identical, the only difference is that BLPOP pops the
	 * element from the left (head) of the list, and BRPOP pops from the right
	 * (tail).
	 * <p/>
	 * <b>Non blocking behavior</b>
	 * <p/>
	 * When BLPOP is called, if at least one of the specified keys contain a non
	 * empty list, an element is popped from the head of the list and returned to
	 * the caller together with the name of the key (BLPOP returns a two elements
	 * array, the first element is the key, the second the popped value).
	 * <p/>
	 * Keys are scanned from left to right, so for instance if you issue BLPOP list1
	 * list2 list3 0 against a dataset where list1 does not exist but list2 and
	 * list3 contain non empty lists, BLPOP guarantees to return an element from the
	 * list stored at list2 (since it is the first non empty list starting from the
	 * left).
	 * <p/>
	 * <b>Blocking behavior</b>
	 * <p/>
	 * If none of the specified keys exist or contain non empty lists, BLPOP blocks
	 * until some other client performs a LPUSH or an RPUSH operation against one of
	 * the lists.
	 * <p/>
	 * Once new data is present on one of the lists, the client finally returns with
	 * the name of the key unblocking it and the popped value.
	 * <p/>
	 * When blocking, if a non-zero timeout is specified, the client will unblock
	 * returning a nil special value if the specified amount of seconds passed
	 * without a push operation against at least one of the specified keys.
	 * <p/>
	 * The timeout argument is interpreted as an integer value. A timeout of zero
	 * means instead to block forever.
	 * <p/>
	 * <b>Multiple clients blocking for the same keys</b>
	 * <p/>
	 * Multiple clients can block for the same key. They are put into a queue, so
	 * the first to be served will be the one that started to wait earlier, in a
	 * first-blpopping first-served fashion.
	 * <p/>
	 * <b>blocking POP inside a MULTI/EXEC transaction</b>
	 * <p/>
	 * BLPOP and BRPOP can be used with pipelining (sending multiple commands and
	 * reading the replies in batch), but it does not make sense to use BLPOP or
	 * BRPOP inside a MULTI/EXEC block (a Redis transaction).
	 * <p/>
	 * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to return a
	 * multi-bulk nil reply, exactly what happens when the timeout is reached. If
	 * you like science fiction, think at it like if inside MULTI/EXEC the time will
	 * flow at infinite speed :)
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param timeout
	 * @param keys
	 * @return BLPOP returns a two-elements array via a multi bulk reply in order to
	 *         return both the unblocking key and the popped value.
	 *         <p/>
	 *         When a non-zero timeout is specified, and the BLPOP operation timed
	 *         out, the return value is a nil multi bulk reply. Most client values
	 *         will return false or nil accordingly to the programming language
	 *         used.
	 * @see #blpop(int, byte[]...)
	 */
	public List<byte[]> brpop(final int timeout, final byte[]... keys) {
		return brpop(getArgsAddTimeout(timeout, keys));
	}

	public List<byte[]> blpop(byte[]... args) {
		checkIsInMulti();
		try {
			return client.getBinaryMultiBulkReply(getResultSet(client.blpop(args)));
		} finally {
		}
	}

	public List<byte[]> brpop(byte[]... args) {
		checkIsInMulti();
		// client.setTimeoutInfinite();
		try {
			//return client.getBinaryMultiBulkReply(getResultSet(client.sendCommand(Command.BLPOP, 0, args)));
			byte[] buff = client.brpop(args);
			if (buff == null)
				return null;
			
			byte[] resultBuf = getResultSet(buff);
			if (resultBuf == null)
				return null;
			
			return client.getBinaryMultiBulkReply(resultBuf);
		} finally {
			// client.rollbackTimeout();
		}
	}

	/**
	 * Request for authentication in a password protected Redis server. A Redis
	 * server can be instructed to require a password before to allow clients to
	 * issue commands. This is done using the requirepass directive in the Redis
	 * configuration file. If the password given by the client is correct the server
	 * replies with an OK status code reply and starts accepting commands from the
	 * client. Otherwise an error is returned and the clients needs to try a new
	 * password. Note that for the high performance nature of Redis it is possible
	 * to try a lot of passwords in parallel in very short time, so make sure to
	 * generate a strong and very long password so that this attack is infeasible.
	 *
	 * @param password
	 * @return Status code reply
	 */
	public String auth(final String password) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.auth(password)));
	}

	// public Pipeline pipelined() {
	// pipeline = new Pipeline();
	// pipeline.setClient(client);
	// return pipeline;
	// }

	public Long zcount(final byte[] key, final double min, final double max) {
		return zcount(key, toByteArray(min), toByteArray(max));
	}

	public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zcount(key, min, max)));
	}

	/**
	 * Return the all the elements in the sorted set at key with a score between min
	 * and max (including elements with score equal to min or max).
	 * <p/>
	 * The elements having the same score are returned sorted lexicographically as
	 * ASCII strings (this follows from a property of Redis sorted sets and does not
	 * involve further computation).
	 * <p/>
	 * Using the optional {@link #zrangeByScore(byte[], double, double, int, int)
	 * LIMIT} it's possible to get only a range of the matching elements in an
	 * SQL-alike way. Note that if offset is large the commands needs to traverse
	 * the list for offset elements and this adds up to the O(M) figure.
	 * <p/>
	 * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
	 * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead of
	 * returning the actual elements in the specified interval, it just returns the
	 * number of matching elements.
	 * <p/>
	 * <b>Exclusive intervals and infinity</b>
	 * <p/>
	 * min and max can be -inf and +inf, so that you are not required to know what's
	 * the greatest or smallest element in order to take, for instance, elements "up
	 * to a given value".
	 * <p/>
	 * Also while the interval is for default closed (inclusive) it's possible to
	 * specify open intervals prefixing the score with a "(" character, so for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (1.3 5}
	 * <p/>
	 * Will return all the values with score &gt; 1.3 and &lt;= 5, while for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (5 (10}
	 * <p/>
	 * Will return all the values with score &gt; 5 and &lt; 10 (5 and 10 excluded).
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))+O(M) with N being the number of elements in the sorted set and M
	 * the number of elements returned by the command, so if M is constant (for
	 * instance you always ask for the first ten elements with LIMIT) you can
	 * consider it O(log(N))
	 *
	 * @param key
	 * @param min
	 * @param max
	 * @return Multi bulk reply specifically a list of elements in the specified
	 *         score range.
	 * @see #zrangeByScore(byte[], double, double)
	 * @see #zrangeByScore(byte[], double, double, int, int)
	 * @see #zrangeByScoreWithScores(byte[], double, double)
	 * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
	 * @see #zcount(byte[], double, double)
	 */
	public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
		return zrangeByScore(key, toByteArray(min), toByteArray(max));
	}

	public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrangeByScore(key, min, max))));
	}

	/**
	 * Return the all the elements in the sorted set at key with a score between min
	 * and max (including elements with score equal to min or max).
	 * <p/>
	 * The elements having the same score are returned sorted lexicographically as
	 * ASCII strings (this follows from a property of Redis sorted sets and does not
	 * involve further computation).
	 * <p/>
	 * Using the optional {@link #zrangeByScore(byte[], double, double, int, int)
	 * LIMIT} it's possible to get only a range of the matching elements in an
	 * SQL-alike way. Note that if offset is large the commands needs to traverse
	 * the list for offset elements and this adds up to the O(M) figure.
	 * <p/>
	 * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
	 * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead of
	 * returning the actual elements in the specified interval, it just returns the
	 * number of matching elements.
	 * <p/>
	 * <b>Exclusive intervals and infinity</b>
	 * <p/>
	 * min and max can be -inf and +inf, so that you are not required to know what's
	 * the greatest or smallest element in order to take, for instance, elements "up
	 * to a given value".
	 * <p/>
	 * Also while the interval is for default closed (inclusive) it's possible to
	 * specify open intervals prefixing the score with a "(" character, so for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (1.3 5}
	 * <p/>
	 * Will return all the values with score &gt; 1.3 and &lt;= 5, while for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (5 (10}
	 * <p/>
	 * Will return all the values with score &gt; 5 and &lt; 10 (5 and 10 excluded).
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))+O(M) with N being the number of elements in the sorted set and M
	 * the number of elements returned by the command, so if M is constant (for
	 * instance you always ask for the first ten elements with LIMIT) you can
	 * consider it O(log(N))
	 *
	 * @param key
	 * @param min
	 * @param max
	 * @return Multi bulk reply specifically a list of elements in the specified
	 *         score range.
	 * @see #zrangeByScore(byte[], double, double)
	 * @see #zrangeByScore(byte[], double, double, int, int)
	 * @see #zrangeByScoreWithScores(byte[], double, double)
	 * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
	 * @see #zcount(byte[], double, double)
	 */
	public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max, final int offset,
			final int count) {
		return zrangeByScore(key, toByteArray(min), toByteArray(max), offset, count);
	}

	public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max, final int offset,
			final int count) {
		checkIsInMulti();
		return SetFromList
				.of(client.getBinaryMultiBulkReply(getResultSet(client.zrangeByScore(key, min, max, offset, count))));
	}

	/**
	 * Return the all the elements in the sorted set at key with a score between min
	 * and max (including elements with score equal to min or max).
	 * <p/>
	 * The elements having the same score are returned sorted lexicographically as
	 * ASCII strings (this follows from a property of Redis sorted sets and does not
	 * involve further computation).
	 * <p/>
	 * Using the optional {@link #zrangeByScore(byte[], double, double, int, int)
	 * LIMIT} it's possible to get only a range of the matching elements in an
	 * SQL-alike way. Note that if offset is large the commands needs to traverse
	 * the list for offset elements and this adds up to the O(M) figure.
	 * <p/>
	 * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
	 * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead of
	 * returning the actual elements in the specified interval, it just returns the
	 * number of matching elements.
	 * <p/>
	 * <b>Exclusive intervals and infinity</b>
	 * <p/>
	 * min and max can be -inf and +inf, so that you are not required to know what's
	 * the greatest or smallest element in order to take, for instance, elements "up
	 * to a given value".
	 * <p/>
	 * Also while the interval is for default closed (inclusive) it's possible to
	 * specify open intervals prefixing the score with a "(" character, so for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (1.3 5}
	 * <p/>
	 * Will return all the values with score &gt; 1.3 and &lt;= 5, while for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (5 (10}
	 * <p/>
	 * Will return all the values with score &gt; 5 and &lt; 10 (5 and 10 excluded).
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))+O(M) with N being the number of elements in the sorted set and M
	 * the number of elements returned by the command, so if M is constant (for
	 * instance you always ask for the first ten elements with LIMIT) you can
	 * consider it O(log(N))
	 *
	 * @param key
	 * @param min
	 * @param max
	 * @return Multi bulk reply specifically a list of elements in the specified
	 *         score range.
	 * @see #zrangeByScore(byte[], double, double)
	 * @see #zrangeByScore(byte[], double, double, int, int)
	 * @see #zrangeByScoreWithScores(byte[], double, double)
	 * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
	 * @see #zcount(byte[], double, double)
	 */
	public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
		return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max));
	}

	public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
		checkIsInMulti();
		return getBinaryTupledSet(getResultSet(client.zrangeByScoreWithScores(key, min, max)));
	}

	/**
	 * Return the all the elements in the sorted set at key with a score between min
	 * and max (including elements with score equal to min or max).
	 * <p/>
	 * The elements having the same score are returned sorted lexicographically as
	 * ASCII strings (this follows from a property of Redis sorted sets and does not
	 * involve further computation).
	 * <p/>
	 * Using the optional {@link #zrangeByScore(byte[], double, double, int, int)
	 * LIMIT} it's possible to get only a range of the matching elements in an
	 * SQL-alike way. Note that if offset is large the commands needs to traverse
	 * the list for offset elements and this adds up to the O(M) figure.
	 * <p/>
	 * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
	 * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead of
	 * returning the actual elements in the specified interval, it just returns the
	 * number of matching elements.
	 * <p/>
	 * <b>Exclusive intervals and infinity</b>
	 * <p/>
	 * min and max can be -inf and +inf, so that you are not required to know what's
	 * the greatest or smallest element in order to take, for instance, elements "up
	 * to a given value".
	 * <p/>
	 * Also while the interval is for default closed (inclusive) it's possible to
	 * specify open intervals prefixing the score with a "(" character, so for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (1.3 5}
	 * <p/>
	 * Will return all the values with score &gt; 1.3 and &lt;= 5, while for
	 * instance:
	 * <p/>
	 * {@code ZRANGEBYSCORE zset (5 (10}
	 * <p/>
	 * Will return all the values with score &gt; 5 and &lt; 10 (5 and 10 excluded).
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))+O(M) with N being the number of elements in the sorted set and M
	 * the number of elements returned by the command, so if M is constant (for
	 * instance you always ask for the first ten elements with LIMIT) you can
	 * consider it O(log(N))
	 *
	 * @param key
	 * @param min
	 * @param max
	 * @return Multi bulk reply specifically a list of elements in the specified
	 *         score range.
	 * @see #zrangeByScore(byte[], double, double)
	 * @see #zrangeByScore(byte[], double, double, int, int)
	 * @see #zrangeByScoreWithScores(byte[], double, double)
	 * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
	 * @see #zcount(byte[], double, double)
	 */
	public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max, final int offset,
			final int count) {
		return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max), offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max, final int offset,
			final int count) {
		checkIsInMulti();
		return getBinaryTupledSet(getResultSet(client.zrangeByScoreWithScores(key, min, max, offset, count)));
	}

	private Set<Tuple> getBinaryTupledSet(byte[] resp) {
		checkIsInMulti();
		List<byte[]> membersWithScores = client.getBinaryMultiBulkReply(resp);
		if (membersWithScores.size() == 0) {
			return Collections.emptySet();
		}
		Set<Tuple> set = new LinkedHashSet<Tuple>(membersWithScores.size() / 2, 1.0f);
		Iterator<byte[]> iterator = membersWithScores.iterator();
		while (iterator.hasNext()) {
			set.add(new Tuple(iterator.next(), Double.valueOf(SafeEncoder.encode(iterator.next()))));
		}
		return set;
	}

	public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min) {
		return zrevrangeByScore(key, toByteArray(max), toByteArray(min));
	}

	public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrevrangeByScore(key, max, min))));
	}

	public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min, final int offset,
			final int count) {
		return zrevrangeByScore(key, toByteArray(max), toByteArray(min), offset, count);
	}

	public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min, final int offset,
			final int count) {
		checkIsInMulti();
		return SetFromList.of(
				client.getBinaryMultiBulkReply(getResultSet(client.zrevrangeByScore(key, max, min, offset, count))));
	}

	public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {
		return zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min));
	}

	public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min, final int offset,
			final int count) {
		return zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min), offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
		checkIsInMulti();
		return getBinaryTupledSet(getResultSet(client.zrevrangeByScoreWithScores(key, max, min)));
	}

	public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min, final int offset,
			final int count) {
		checkIsInMulti();
		return getBinaryTupledSet(getResultSet(client.zrevrangeByScoreWithScores(key, max, min, offset, count)));
	}

	/**
	 * Remove all elements in the sorted set at key with rank between start and end.
	 * Start and end are 0-based with rank 0 being the element with the lowest
	 * score. Both start and end can be negative numbers, where they indicate
	 * offsets starting at the element with the highest rank. For example: -1 is the
	 * element with the highest score, -2 the element with the second highest score
	 * and so forth.
	 * <p/>
	 * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of elements in
	 * the sorted set and M the number of elements removed by the operation
	 */
	public Long zremrangeByRank(final byte[] key, final long start, final long end) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zremrangeByRank(key, start, end)));
	}

	/**
	 * Remove all the elements in the sorted set at key with a score between min and
	 * max (including elements with score equal to min or max).
	 * <p/>
	 * <b>Time complexity:</b>
	 * <p/>
	 * O(log(N))+O(M) with N being the number of elements in the sorted set and M
	 * the number of elements removed by the operation
	 *
	 * @param key
	 * @param start
	 * @param end
	 * @return Integer reply, specifically the number of elements removed.
	 */
	public Long zremrangeByScore(final byte[] key, final double start, final double end) {
		return zremrangeByScore(key, toByteArray(start), toByteArray(end));
	}

	public Long zremrangeByScore(final byte[] key, final byte[] start, final byte[] end) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zremrangeByScore(key, start, end)));
	}

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through kN,
	 * and stores it at dstkey. It is mandatory to provide the number of input keys
	 * N, before passing the input keys and the other (optional) arguments.
	 * <p/>
	 * As the terms imply, the {@link #zinterstore(byte[], byte[]...)} ZINTERSTORE}
	 * command requires an element to be present in each of the given inputs to be
	 * inserted in the result. The {@link #zunionstore(byte[], byte[]...)} command
	 * inserts all elements across all inputs.
	 * <p/>
	 * Using the WEIGHTS option, it is possible to add weight to each input sorted
	 * set. This means that the score of each element in the sorted set is first
	 * multiplied by this weight before being passed to the aggregation. When this
	 * option is not given, all weights default to 1.
	 * <p/>
	 * With the AGGREGATE option, it's possible to specify how the results of the
	 * union or intersection are aggregated. This option defaults to SUM, where the
	 * score of an element is summed across the inputs where it exists. When this
	 * option is set to be either MIN or MAX, the resulting set will contain the
	 * minimum or maximum score of an element across the inputs where it exists.
	 * <p/>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the sizes
	 * of the input sorted sets, and M being the number of elements in the resulting
	 * sorted set
	 *
	 * @param dstkey
	 * @param sets
	 * @return Integer reply, specifically the number of elements in the sorted set
	 *         at dstkey
	 * @see #zunionstore(byte[], byte[]...)
	 * @see #zunionstore(byte[], ZParams, byte[]...)
	 * @see #zinterstore(byte[], byte[]...)
	 * @see #zinterstore(byte[], ZParams, byte[]...)
	 */
	public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zunionstore(dstkey, sets)));
	}

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through kN,
	 * and stores it at dstkey. It is mandatory to provide the number of input keys
	 * N, before passing the input keys and the other (optional) arguments.
	 * <p/>
	 * As the terms imply, the {@link #zinterstore(byte[], byte[]...) ZINTERSTORE}
	 * command requires an element to be present in each of the given inputs to be
	 * inserted in the result. The {@link #zunionstore(byte[], byte[]...)
	 * ZUNIONSTORE} command inserts all elements across all inputs.
	 * <p/>
	 * Using the WEIGHTS option, it is possible to add weight to each input sorted
	 * set. This means that the score of each element in the sorted set is first
	 * multiplied by this weight before being passed to the aggregation. When this
	 * option is not given, all weights default to 1.
	 * <p/>
	 * With the AGGREGATE option, it's possible to specify how the results of the
	 * union or intersection are aggregated. This option defaults to SUM, where the
	 * score of an element is summed across the inputs where it exists. When this
	 * option is set to be either MIN or MAX, the resulting set will contain the
	 * minimum or maximum score of an element across the inputs where it exists.
	 * <p/>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the sizes
	 * of the input sorted sets, and M being the number of elements in the resulting
	 * sorted set
	 *
	 * @param dstkey
	 * @param sets
	 * @param params
	 * @return Integer reply, specifically the number of elements in the sorted set
	 *         at dstkey
	 * @see #zunionstore(byte[], byte[]...)
	 * @see #zunionstore(byte[], ZParams, byte[]...)
	 * @see #zinterstore(byte[], byte[]...)
	 * @see #zinterstore(byte[], ZParams, byte[]...)
	 */
	public Long zunionstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zunionstore(dstkey, params, sets)));
	}

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through kN,
	 * and stores it at dstkey. It is mandatory to provide the number of input keys
	 * N, before passing the input keys and the other (optional) arguments.
	 * <p/>
	 * As the terms imply, the {@link #zinterstore(byte[], byte[]...) ZINTERSTORE}
	 * command requires an element to be present in each of the given inputs to be
	 * inserted in the result. The {@link #zunionstore(byte[], byte[]...)
	 * ZUNIONSTORE} command inserts all elements across all inputs.
	 * <p/>
	 * Using the WEIGHTS option, it is possible to add weight to each input sorted
	 * set. This means that the score of each element in the sorted set is first
	 * multiplied by this weight before being passed to the aggregation. When this
	 * option is not given, all weights default to 1.
	 * <p/>
	 * With the AGGREGATE option, it's possible to specify how the results of the
	 * union or intersection are aggregated. This option defaults to SUM, where the
	 * score of an element is summed across the inputs where it exists. When this
	 * option is set to be either MIN or MAX, the resulting set will contain the
	 * minimum or maximum score of an element across the inputs where it exists.
	 * <p/>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the sizes
	 * of the input sorted sets, and M being the number of elements in the resulting
	 * sorted set
	 *
	 * @param dstkey
	 * @param sets
	 * @return Integer reply, specifically the number of elements in the sorted set
	 *         at dstkey
	 * @see #zunionstore(byte[], byte[]...)
	 * @see #zunionstore(byte[], ZParams, byte[]...)
	 * @see #zinterstore(byte[], byte[]...)
	 * @see #zinterstore(byte[], ZParams, byte[]...)
	 */
	public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zinterstore(dstkey, sets)));
	}

	/**
	 * Creates a union or intersection of N sorted sets given by keys k1 through kN,
	 * and stores it at dstkey. It is mandatory to provide the number of input keys
	 * N, before passing the input keys and the other (optional) arguments.
	 * <p/>
	 * As the terms imply, the {@link #zinterstore(byte[], byte[]...) ZINTERSTORE}
	 * command requires an element to be present in each of the given inputs to be
	 * inserted in the result. The {@link #zunionstore(byte[], byte[]...)
	 * ZUNIONSTORE} command inserts all elements across all inputs.
	 * <p/>
	 * Using the WEIGHTS option, it is possible to add weight to each input sorted
	 * set. This means that the score of each element in the sorted set is first
	 * multiplied by this weight before being passed to the aggregation. When this
	 * option is not given, all weights default to 1.
	 * <p/>
	 * With the AGGREGATE option, it's possible to specify how the results of the
	 * union or intersection are aggregated. This option defaults to SUM, where the
	 * score of an element is summed across the inputs where it exists. When this
	 * option is set to be either MIN or MAX, the resulting set will contain the
	 * minimum or maximum score of an element across the inputs where it exists.
	 * <p/>
	 * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the sizes
	 * of the input sorted sets, and M being the number of elements in the resulting
	 * sorted set
	 *
	 * @param dstkey
	 * @param sets
	 * @param params
	 * @return Integer reply, specifically the number of elements in the sorted set
	 *         at dstkey
	 * @see #zunionstore(byte[], byte[]...)
	 * @see #zunionstore(byte[], ZParams, byte[]...)
	 * @see #zinterstore(byte[], byte[]...)
	 * @see #zinterstore(byte[], ZParams, byte[]...)
	 */
	public Long zinterstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zinterstore(dstkey, params, sets)));
	}

	@Override
	public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.zlexcount(key, min, max)));
	}

	@Override
	public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
		checkIsInMulti();
		return SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrangeByLex(key, min, max))));
	}

	@Override
	public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max, final int offset,
			final int count) {
		checkIsInMulti();
		return SetFromList
				.of(client.getBinaryMultiBulkReply(getResultSet(client.zrangeByLex(key, min, max, offset, count))));
	}

	// @Override
	// public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
	// throw new JedisException("��֧�ֵ�");
	//// checkIsInMulti();
	//// return
	// SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrevrangeByLex(key,
	// max, min))));
	// }

	// @Override
	// public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min,
	// int offset, int count) {
	// checkIsInMulti();
	// return
	// SetFromList.of(client.getBinaryMultiBulkReply(getResultSet(client.zrevrangeByLex(key,
	// max, min, offset, count))));
	// }

	@Override
	public Long zremrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
		checkIsInMulti();
		// TODO 未取到返回值,要补充
		return client.getIntegerReply(null);
	}

	/**
	 * Synchronously save the DB on disk.
	 * <p/>
	 * Save the whole dataset on disk (this means that all the databases are saved,
	 * as well as keys with an EXPIRE set (the expire is preserved). The server
	 * hangs while the saving is not completed, no connection is served in the
	 * meanwhile. An OK code is returned when the DB was fully stored in disk.
	 * <p/>
	 * The background variant of this command is {@link #bgsave() BGSAVE} that is
	 * able to perform the saving in the background while the server continues
	 * serving other clients.
	 * <p/>
	 *
	 * @return Status code reply
	 */
	public String save() {
		return client.getStatusCodeReply(getResultSet(client.save()));
	}

	/**
	 * Asynchronously save the DB on disk.
	 * <p/>
	 * Save the DB in background. The OK code is immediately returned. Redis forks,
	 * the parent continues to server the clients, the child saves the DB on disk
	 * then exit. A client my be able to check if the operation succeeded using the
	 * LASTSAVE command.
	 *
	 * @return Status code reply
	 */
	public String bgsave() {
		return client.getStatusCodeReply(getResultSet(client.bgsave()));
	}

	/**
	 * Rewrite the append only file in background when it gets too big. Please for
	 * detailed information about the Redis Append Only File check the
	 * <a href="http://redis.io/topics/persistence#append-only-file">Append Only
	 * File Howto</a>.
	 * <p/>
	 * BGREWRITEAOF rewrites the Append Only File in background when it gets too
	 * big. The Redis Append Only File is a Journal, so every operation modifying
	 * the dataset is logged in the Append Only File (and replayed at startup). This
	 * means that the Append Only File always grows. In order to rebuild its content
	 * the BGREWRITEAOF creates a new version of the append only file starting
	 * directly form the dataset in memory in order to guarantee the generation of
	 * the minimal number of commands needed to rebuild the database.
	 * <p/>
	 *
	 * @return Status code reply
	 */
	public String bgrewriteaof() {
		return client.getStatusCodeReply(getResultSet(client.bgrewriteaof()));
	}

	/**
	 * Return the UNIX time stamp of the last successfully saving of the dataset on
	 * disk.
	 * <p/>
	 * Return the UNIX TIME of the last DB save executed with success. A client may
	 * check if a {@link #bgsave() BGSAVE} command succeeded reading the LASTSAVE
	 * value, then issuing a BGSAVE command and checking at regular intervals every
	 * N seconds if LASTSAVE changed.
	 *
	 * @return Integer reply, specifically an UNIX time stamp.
	 */
	public Long lastsave() {
		return client.getIntegerReply(getResultSet(client.lastsave()));
	}

	/**
	 * Synchronously save the DB on disk, then shutdown the server.
	 * <p/>
	 * Stop all the clients, save the DB, then quit the server. This commands makes
	 * sure that the DB is switched off without the lost of any data. This is not
	 * guaranteed if the client uses simply {@link #save() SAVE} and then
	 * {@link #quit() QUIT} because other clients may alter the DB data between the
	 * two commands.
	 *
	 * @return Status code reply on error. On success nothing is returned since the
	 *         server quits and the connection is closed.
	 */
	public String shutdown() {
		String status;
		try {
			status = client.getStatusCodeReply(getResultSet(client.shutdown()));

		} catch (RedisException ex) {
			status = null;
		}
		return status;
	}

	/**
	 * Provide information and statistics about the server.
	 * <p/>
	 * The info command returns different information and statistics about the
	 * server in an format that's simple to parse by computers and easy to read by
	 * humans.
	 * <p/>
	 * <b>Format of the returned String:</b>
	 * <p/>
	 * All the fields are in the form field:value
	 * <p/>
	 * 
	 * <pre>
	 * edis_version:0.07
	 * connected_clients:1
	 * connected_slaves:0
	 * used_memory:3187
	 * changes_since_last_save:0
	 * last_save_time:1237655729
	 * total_connections_received:1
	 * total_commands_processed:1
	 * uptime_in_seconds:25
	 * uptime_in_days:0
	 * </pre>
	 * <p/>
	 * <b>Notes</b>
	 * <p/>
	 * used_memory is returned in bytes, and is the total number of bytes allocated
	 * by the program using malloc.
	 * <p/>
	 * uptime_in_days is redundant since the uptime in seconds contains already the
	 * full uptime information, this field is only mainly present for humans.
	 * <p/>
	 * changes_since_last_save does not refer to the number of key changes, but to
	 * the number of operations that produced some kind of change in the dataset.
	 * <p/>
	 *
	 * @return Bulk reply
	 */
	public String info() {
		return client.getBulkReply(getResultSet(client.info()));
	}

	public String info(final String section) {
		return client.getBulkReply(getResultSet(client.info(section)));
	}

	/**
	 * Dump all the received requests in real time.
	 * <p/>
	 * MONITOR is a debugging command that outputs the whole sequence of commands
	 * received by the Redis server. is very handy in order to understand what is
	 * happening into the database. This command is used directly via telnet.
	 *
	 * @param jedisMonitor
	 */
	// public void monitor(final JedisMonitor jedisMonitor) {
	// client.getStatusCodeReply(getResultSet(client.monitor()));
	// jedisMonitor.proceed(client);
	// }

	/**
	 * Change the replication settings.
	 * <p/>
	 * The SLAVEOF command can change the replication settings of a slave on the
	 * fly. If a Redis server is arleady acting as slave, the command SLAVEOF NO ONE
	 * will turn off the replicaiton turning the Redis server into a MASTER. In the
	 * proper form SLAVEOF hostname port will make the server a slave of the
	 * specific server listening at the specified hostname and port.
	 * <p/>
	 * If a server is already a slave of some master, SLAVEOF hostname port will
	 * stop the replication against the old server and start the synchrnonization
	 * against the new one discarding the old dataset.
	 * <p/>
	 * The form SLAVEOF no one will stop replication turning the server into a
	 * MASTER but will not discard the replication. So if the old master stop
	 * working it is possible to turn the slave into a master and set the
	 * application to use the new master in read/write. Later when the other Redis
	 * server will be fixed it can be configured in order to work as slave.
	 * <p/>
	 *
	 * @param host
	 * @param port
	 * @return Status code reply
	 */
	public String slaveof(final String host, final int port) {
		return client.getStatusCodeReply(getResultSet(client.slaveof(host, port)));
	}

	public String slaveofNoOne() {
		return client.getStatusCodeReply(getResultSet(client.slaveofNoOne()));
	}

	/**
	 * Retrieve the configuration of a running Redis server. Not all the
	 * configuration parameters are supported.
	 * <p/>
	 * CONFIG GET returns the current configuration parameters. This sub command
	 * only accepts a single argument, that is glob style pattern. All the
	 * configuration parameters matching this parameter are reported as a list of
	 * key-value pairs.
	 * <p/>
	 * <b>Example:</b>
	 * <p/>
	 * 
	 * <pre>
	 * $ redis-cli config get '*'
	 * 1. "dbfilename"
	 * 2. "dump.rdb"
	 * 3. "requirepass"
	 * 4. (nil)
	 * 5. "masterauth"
	 * 6. (nil)
	 * 7. "maxmemory"
	 * 8. "0\n"
	 * 9. "appendfsync"
	 * 10. "everysec"
	 * 11. "save"
	 * 12. "3600 1 300 100 60 10000"
	 *
	 * $ redis-cli config get 'm*'
	 * 1. "masterauth"
	 * 2. (nil)
	 * 3. "maxmemory"
	 * 4. "0\n"
	 * </pre>
	 *
	 * @param pattern
	 * @return Bulk reply.
	 */
	public List<byte[]> configGet(final byte[] pattern) {
		return client.getBinaryMultiBulkReply(getResultSet(client.configGet(pattern)));
	}

	/**
	 * Reset the stats returned by INFO
	 *
	 * @return
	 */
	public String configResetStat() {
		return client.getStatusCodeReply(getResultSet(client.configResetStat()));
	}

	/**
	 * Alter the configuration of a running Redis server. Not all the configuration
	 * parameters are supported.
	 * <p/>
	 * The list of configuration parameters supported by CONFIG SET can be obtained
	 * issuing a {@link #configGet(byte[]) CONFIG GET *} command.
	 * <p/>
	 * The configuration set using CONFIG SET is immediately loaded by the Redis
	 * server that will start acting as specified starting from the next command.
	 * <p/>
	 * <b>Parameters value format</b>
	 * <p/>
	 * The value of the configuration parameter is the same as the one of the same
	 * parameter in the Redis configuration file, with the following exceptions:
	 * <p/>
	 * <ul>
	 * <li>The save paramter is a list of space-separated integers. Every pair of
	 * integers specify the time and number of changes limit to trigger a save. For
	 * instance the command CONFIG SET save "3600 10 60 10000" will configure the
	 * server to issue a background saving of the RDB file every 3600 seconds if
	 * there are at least 10 changes in the dataset, and every 60 seconds if there
	 * are at least 10000 changes. To completely disable automatic snapshots just
	 * set the parameter as an empty string.
	 * <li>All the integer parameters representing memory are returned and accepted
	 * only using bytes as unit.
	 * </ul>
	 *
	 * @param parameter
	 * @param value
	 * @return Status code reply
	 */
	public byte[] configSet(final byte[] parameter, final byte[] value) {
		return client.getBinaryBulkReply(getResultSet(client.configSet(parameter, value)));
	}

	public boolean isConnected() {
		return client.connection.isConnected();
	}

	public Long strlen(final byte[] key) {
		return client.getIntegerReply(getResultSet(client.strlen(key)));
	}

	public void sync() {
		client.sync();
	}

	public Long lpushx(final byte[] key, final byte[]... string) {
		return client.getIntegerReply(getResultSet(client.lpushx(key, string)));
	}

	/**
	 * Undo a {@link #expire(byte[], int) expire} at turning the expire key into a
	 * normal key.
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @param key
	 * @return Integer reply, specifically: 1: the key is now persist. 0: the key is
	 *         not persist (only happens when key not set).
	 */
	public Long persist(final byte[] key) {
		return client.getIntegerReply(getResultSet(client.persist(key)));
	}

	public Long rpushx(final byte[] key, final byte[]... string) {
		return client.getIntegerReply(getResultSet(client.rpushx(key, string)));
	}

	public byte[] echo(final byte[] string) {
		return client.getBinaryBulkReply(getResultSet(client.echo(string)));
	}

	public Long linsert(final byte[] key, final LIST_POSITION where, final byte[] pivot, final byte[] value) {
		return client.getIntegerReply(getResultSet(client.linsert(key, where, pivot, value)));
	}

	// public String debug(final DebugParams params) {
	// return client.getStatusCodeReply(getResultSet(client.debug(params)));
	// }

	/**
	 * Pop a value from a list, push it to another list and return it; or block
	 * until one is available
	 *
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return the element
	 */
	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		// client.setTimeoutInfinite();
		try {
			return client.getBinaryBulkReply(getResultSet(client.brpoplpush(source, destination, timeout)));
		} finally {
			// client.rollbackTimeout();
		}
	}

	public NBinaryClient getClient() {
		return client;
	}

	/**
	 * Sets or clears the bit at offset in the string value stored at key
	 *
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	public Boolean setbit(byte[] key, long offset, boolean value) {
		return client.getIntegerReply(getResultSet(client.setbit(key, offset, value))) == 1;
	}

	public Boolean setbit(byte[] key, long offset, byte[] value) {
		return client.getIntegerReply(getResultSet(client.setbit(key, offset, value))) == 1;
	}

	/**
	 * Returns the bit value at offset in the string value stored at key
	 *
	 * @param key
	 * @param offset
	 * @return
	 */
	public Boolean getbit(byte[] key, long offset) {
		return client.getIntegerReply(getResultSet(client.getbit(key, offset))) == 1;
	}

	public Long bitpos(final byte[] key, final boolean value) {
		return bitpos(key, value, new BitPosParams(0));
	}

	public Long bitpos(final byte[] key, final boolean value, final BitPosParams params) {
		return client.getIntegerReply(getResultSet(client.bitpos(key, value, params)));
	}

	public Long setrange(byte[] key, long offset, byte[] value) {
		return client.getIntegerReply(getResultSet(client.setrange(key, offset, value)));
	}

	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return client.getBinaryBulkReply(getResultSet(client.getrange(key, startOffset, endOffset)));
	}

	public Long objectRefcount(byte[] key) {
		return client.getIntegerReply(getResultSet(client.objectRefcount(key)));
	}

	public byte[] objectEncoding(byte[] key) {
		return client.getBinaryBulkReply(getResultSet(client.objectEncoding(key)));
	}

	public Long objectIdletime(byte[] key) {
		return client.getIntegerReply(getResultSet(client.objectIdletime(key)));
	}

	public Long bitcount(final byte[] key) {
		return client.getIntegerReply(getResultSet(client.bitcount(key)));
	}

	public Long bitcount(final byte[] key, long start, long end) {
		return client.getIntegerReply(getResultSet(client.bitcount(key, start, end)));
	}

	public Long bitop(BitOP op, final byte[] destKey, byte[]... srcKeys) {
		return client.getIntegerReply(getResultSet(client.bitop(op, destKey, srcKeys)));
	}

	public byte[] dump(final byte[] key) {
		checkIsInMulti();
		return client.getBinaryBulkReply(getResultSet(client.dump(key)));
	}

	public String restore(final byte[] key, final int ttl, final byte[] serializedValue) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.restore(key, ttl, serializedValue)));
	}

	public Long pexpire(final byte[] key, final long milliseconds) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.pexpire(key, milliseconds)));
	}

	public Long pexpireAt(final byte[] key, final long millisecondsTimestamp) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.pexpireAt(key, millisecondsTimestamp)));
	}

	public Long pttl(final byte[] key) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.pttl(key)));
	}

	/**
	 * PSETEX works exactly like {@link #setex(byte[], int, byte[])} }with the sole
	 * difference that the expire time is specified in milliseconds instead of
	 * seconds. Time complexity: O(1)
	 *
	 * @param key
	 * @param milliseconds
	 * @param value
	 * @return Status code reply
	 */
	public String psetex(final byte[] key, final long milliseconds, final byte[] value) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.psetex(key, milliseconds, value)));
	}

	public String set(final byte[] key, final byte[] value, final byte[] nxxx) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.set(key, value, nxxx)));
	}

	public String set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final int time) {
		checkIsInMulti();
		return client.getStatusCodeReply(getResultSet(client.set(key, value, nxxx, expx, time)));
	}

	public String clientKill(final byte[] client) {
		checkIsInMulti();
		return this.client.getStatusCodeReply(getResultSet(this.client.clientKill(client)));
	}

	public String clientGetname() {
		checkIsInMulti();
		return client.getBulkReply(getResultSet(client.clientGetname()));
	}

	public String clientList() {
		checkIsInMulti();
		return client.getBulkReply(getResultSet(client.clientList()));
	}

	public String clientSetname(final byte[] name) {
		checkIsInMulti();
		return client.getBulkReply(getResultSet(client.clientSetname(name)));
	}

	public List<String> time() {
		checkIsInMulti();
		return client.getMultiBulkReply(getResultSet(client.time()));
	}

	public Long waitReplicas(int replicas, long timeout) {
		checkIsInMulti();
		return client.getIntegerReply(getResultSet(client.waitReplicas(replicas, timeout)));
	}

	/**
	 * A decorator to implement Set from List. Assume that given List do not
	 * contains duplicated values. The resulting set displays the same ordering,
	 * concurrency, and performance characteristics as the backing list. This class
	 * should be used only for Redis commands which return Set result.
	 *
	 * @param <E>
	 */
	protected static class SetFromList<E> extends AbstractSet<E> {
		private final List<E> list;

		private SetFromList(List<E> list) {
			if (list == null) {
				throw new NullPointerException("list");
			}
			this.list = list;
		}

		protected static <E> SetFromList<E> of(List<E> list) {
			return new SetFromList<E>(list);
		}

		public void clear() {
			list.clear();
		}

		public int size() {
			return list.size();
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}

		public boolean contains(Object o) {
			return list.contains(o);
		}

		public boolean remove(Object o) {
			return list.remove(o);
		}

		public boolean add(E e) {
			return !contains(e) && list.add(e);
		}

		public Iterator<E> iterator() {
			return list.iterator();
		}

		public Object[] toArray() {
			return list.toArray();
		}

		public <T> T[] toArray(T[] a) {
			return list.toArray(a);
		}

		public String toString() {
			return list.toString();
		}

		public int hashCode() {
			return list.hashCode();
		}

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (!(o instanceof Set)) {
				return false;
			}

			Collection<?> c = (Collection<?>) o;
			if (c.size() != size()) {
				return false;
			}

			return containsAll(c);
		}

		public boolean containsAll(Collection<?> c) {
			return list.containsAll(c);
		}

		public boolean removeAll(Collection<?> c) {
			return list.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return list.retainAll(c);
		}
	}

	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		checkIsInMulti();
		return client.getBinaryMultiBulkReply(getResultSet(client.sort(key, sortingParameters)));
	}

}
