package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MapOutput<K, V> extends CommandOutput<K, V, Map<K, V>> {
    private K key;

    public MapOutput(RedisCodec<K, V> codec) {
        super(codec, new HashMap<K, V>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (key == null) {
            key = codec.decodeKey(bytes);
            return;
        }

        V value = (bytes == null) ? null : codec.decodeValue(bytes);
        output.put(key, value);
        key = null;
    }
}
