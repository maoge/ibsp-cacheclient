package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class StringListOutput<K, V> extends CommandOutput<K, V, List<String>> {
    public StringListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<String>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : decodeAscii(bytes));
    }
}
