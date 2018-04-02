package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.nio.ByteBuffer;

public class BooleanOutput<K, V> extends CommandOutput<K, V, Boolean> {
    public BooleanOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(long integer) {
        output = (integer == 1) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = (bytes != null) ? Boolean.TRUE : Boolean.FALSE;
    }
}
