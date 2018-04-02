package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import static ibsp.cache.client.protocol.Charsets.buffer;

import java.nio.ByteBuffer;

public class StatusOutput<K, V> extends CommandOutput<K, V, String> {
    private static final ByteBuffer OK = buffer("OK");

    public StatusOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = OK.equals(bytes) ? "OK" : "NOT OK";
    }
}
