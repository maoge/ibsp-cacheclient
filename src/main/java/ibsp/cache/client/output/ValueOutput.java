package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;

public class ValueOutput<K, V> extends CommandOutput<K, V, V> {
    public ValueOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = (bytes == null) ? null : codec.decodeValue(bytes);
    }
}
