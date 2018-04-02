package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;

import java.util.ArrayList;
import java.util.List;

public class BooleanListOutput<K, V> extends CommandOutput<K, V, List<Boolean>> {
    public BooleanListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<Boolean>());
    }

    @Override
    public void set(long integer) {
        output.add((integer == 1) ? Boolean.TRUE : Boolean.FALSE);
    }
}
