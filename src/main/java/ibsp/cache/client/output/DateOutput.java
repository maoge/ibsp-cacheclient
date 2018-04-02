package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.util.Date;

public class DateOutput<K, V> extends CommandOutput<K, V, Date> {
    public DateOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(long time) {
        output = new Date(time * 1000);
    }
}
