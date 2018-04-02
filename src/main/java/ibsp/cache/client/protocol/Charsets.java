package ibsp.cache.client.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Charsets {
    public static final Charset ASCII = Charset.forName("US-ASCII");

    public static ByteBuffer buffer(String s) {
        return ByteBuffer.wrap(s.getBytes(ASCII));
    }
}
