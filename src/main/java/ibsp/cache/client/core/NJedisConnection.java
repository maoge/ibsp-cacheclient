package ibsp.cache.client.core;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.connection.AsyncConnection;
import ibsp.cache.client.connection.AsyncConversion;
import ibsp.cache.client.connection.LongConnection;
import ibsp.cache.client.connection.SyncConnection;
import ibsp.cache.client.connection.SyncConversion;
import ibsp.cache.client.exception.RedisConnectionException;
import ibsp.cache.client.exception.RedisDataException;
import ibsp.cache.client.protocol.BuilderFactory;
import ibsp.cache.client.protocol.Protocol;
import ibsp.cache.client.protocol.RedisInputStream;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.utils.ByteUtil;

public class NJedisConnection {

	public static final Logger logger = LoggerFactory.getLogger(NJedisConnection.class);
	protected LongConnection connection;

	public NJedisConnection(String host, int port, boolean isSync) {
		if (isSync) {
			this.connection = new SyncConnection(host, port, new SyncConversion());
		} else {
			this.connection = new AsyncConnection(host, port, new AsyncConversion());
		}
	}

	public NJedisConnection(String host, int port, int timeOut, boolean isSync) {
		if (isSync) {
			this.connection = new SyncConnection(host, port, new SyncConversion(), timeOut);
		} else {
			this.connection = new AsyncConnection(host, port, new AsyncConversion(), timeOut);
		}
	}

	public NJedisConnection(String host, int port, int timeOut, String connectionName, boolean isSync) {
		if (isSync) {
			this.connection = new SyncConnection(host, port, new SyncConversion(), timeOut, connectionName);
		} else {
			this.connection = new AsyncConnection(host, port, new AsyncConversion(), timeOut, connectionName);
		}
	}

	public void setExceptionList(Map<Exception, String> excepts) {
		this.connection.setExceptionList(excepts);
	}

	public String getStatusCodeReply(final byte[] resp1) {
		flush();
		final byte[] dresp = (byte[]) readProtocolWithCheckingBroken(resp1);
		if (null == dresp) {
			return null;
		} else {
			return SafeEncoder.encode(dresp);
		}
	}

	public String getBulkReply(final byte[] result1) {
		final byte[] result = getBinaryBulkReply(result1);
		if (null != result) {
			return SafeEncoder.encode(result);
		} else {
			return null;
		}
	}

	public byte[] getBinaryBulkReply(final byte[] result) {
		return (byte[]) readProtocolWithCheckingBroken(result);
	}

	public Long getIntegerReply(final byte[] result) {
		return (Long) readProtocolWithCheckingBroken(result);
	}

	public List<String> getMultiBulkReply(final byte[] readBuffer) {
		return BuilderFactory.STRING_LIST.build(getBinaryMultiBulkReply(readBuffer));
	}

	@SuppressWarnings("unchecked")
	public List<byte[]> getBinaryMultiBulkReply(final byte[] readBuffer) {
		flush();
		return (List<byte[]>) readProtocolWithCheckingBroken(readBuffer);
	}

	@SuppressWarnings("unchecked")
	public List<Object> getRawObjectMultiBulkReply(final byte[] readBuffer) {
		return (List<Object>) readProtocolWithCheckingBroken(readBuffer);
	}

	public List<Object> getObjectMultiBulkReply(final byte[] readBuffer) {
		return getRawObjectMultiBulkReply(readBuffer);
	}

	@SuppressWarnings("unchecked")
	public List<Long> getIntegerMultiBulkReply(final byte[] readBuffer) {
		flush();
		return (List<Long>) readProtocolWithCheckingBroken(readBuffer);
	}

	public Object getOne(final byte[] readBuffer) {
		flush();
		return readProtocolWithCheckingBroken(readBuffer);
	}

	protected void flush() {
		// try {
		//// outputStream.flush();
		// } catch (IOException ex) {
		// broken = true;
		// throw new JedisConnectionException(ex);
		// }
	}

	protected Object readProtocolWithCheckingBroken(byte[] resp) {
		try {
			return Protocol.read(new RedisInputStream(new ByteArrayInputStream(resp)));
		} catch (RedisConnectionException exc) {
			logger.error("unecpected：" + new String(resp) + ":" + ByteUtil.Bytes2HexString(resp), exc);
			throw exc;
		} catch (RedisDataException edc) {
			logger.error("unecpected：" + new String(resp) + ":" + ByteUtil.Bytes2HexString(resp) + ",message:"
					+ edc.getMessage(), edc);
			throw edc;
		}
	}

	public List<Object> getMany(final int count, byte[] resp) {
		final List<Object> responses = new ArrayList<Object>(count);
		for (int i = 0; i < count; i++) {
			try {
				responses.add(readProtocolWithCheckingBroken(resp));
			} catch (RedisDataException e) {
				responses.add(e);
			}
		}
		return responses;
	}

}
