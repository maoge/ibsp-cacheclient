package ibsp.cache.client.codec;

import java.nio.ByteBuffer;

public abstract class RedisCodec<K, V> {

	public abstract K decodeKey(ByteBuffer bytes);

	public abstract V decodeValue(ByteBuffer bytes);

	public abstract byte[] encodeKey(K key);

	public abstract byte[] encodeValue(V value);
}
