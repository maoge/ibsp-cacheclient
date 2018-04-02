package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class ValueSetOutput<K, V> extends CommandOutput<K, V, Set<V>> {
    public ValueSetOutput(RedisCodec<K, V> codec) {
        super(codec, new HashSet<V>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : codec.decodeValue(bytes));
    }
}
