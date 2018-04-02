package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class KeySetOutput<K, V> extends CommandOutput<K, V, Set<K>> {
    public KeySetOutput(RedisCodec<K, V> codec) {
        super(codec, new HashSet<K>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : codec.decodeKey(bytes));
    }
}
