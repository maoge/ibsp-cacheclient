package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KeyListOutput<K, V> extends CommandOutput<K, V, List<K>> {
    public KeyListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<K>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(codec.decodeKey(bytes));
    }
}
