package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;

import static java.lang.Double.parseDouble;

public class DoubleOutput<K, V> extends CommandOutput<K, V, Double> {
    public DoubleOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = (bytes == null) ? null : parseDouble(decodeAscii(bytes));
    }
}
