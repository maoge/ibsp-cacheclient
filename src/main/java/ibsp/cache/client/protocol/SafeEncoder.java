package ibsp.cache.client.protocol;

import java.io.UnsupportedEncodingException;

import ibsp.cache.client.exception.RedisDataException;
import ibsp.cache.client.exception.RedisException;

public class SafeEncoder {

	public static byte[][] encodeMany(final String... strs) {
		byte[][] many = new byte[strs.length][];
		for (int i = 0; i < strs.length; i++) {
			many[i] = encode(strs[i]);
		}
		return many;
	}

	public static byte[] encode(final String str) {
		try {
			if (str == null) {
				throw new RedisDataException("value sent to redis cannot be null");
			}
			return str.getBytes(Protocol.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RedisException(e);
		}
	}

	public static String encode(final byte[] data) {
		try {
			return new String(data, Protocol.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RedisException(e);
		}
	}

}
