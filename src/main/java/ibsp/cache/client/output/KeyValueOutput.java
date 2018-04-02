package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;
import ibsp.cache.client.utils.KeyValue;

import java.nio.ByteBuffer;

public class KeyValueOutput<K, V> extends CommandOutput<K, V, KeyValue<K, V>> {
    private K key;

    public KeyValueOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (bytes != null) {
            if (key == null) {
                key = codec.decodeKey(bytes);
            } else {
                V value = codec.decodeValue(bytes);
                output = new KeyValue<K, V>(key, value);
            }
        }
    }
}
