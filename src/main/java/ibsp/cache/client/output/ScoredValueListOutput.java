package ibsp.cache.client.output;

import ibsp.cache.client.codec.RedisCodec;
import ibsp.cache.client.protocol.CommandOutput;
import ibsp.cache.client.protocol.ScoredValue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ScoredValueListOutput<K, V> extends CommandOutput<K, V, List<ScoredValue<V>>> {
    private V value;

    public ScoredValueListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<ScoredValue<V>>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (value == null) {
            value = codec.decodeValue(bytes);
            return;
        }

        double score = Double.parseDouble(decodeAscii(bytes));
        output.add(new ScoredValue<V>(score, value));
        value = null;
    }
}
