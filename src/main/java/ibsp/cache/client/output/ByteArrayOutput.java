package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;

public class ByteArrayOutput<K, V> extends CommandOutput<K, V, byte[]> {
    public ByteArrayOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (bytes != null) {
            output = new byte[bytes.remaining()];
            bytes.get(output);
        }
    }
}
