package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ValueListOutput<K, V> extends CommandOutput<K, V, List<V>> {
    public ValueListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<V>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : codec.decodeValue(bytes));
    }
}
