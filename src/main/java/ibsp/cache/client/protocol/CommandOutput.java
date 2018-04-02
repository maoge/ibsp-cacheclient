package ibsp.cache.client.protocol;

import java.nio.ByteBuffer;

import ibsp.cache.client.codec.RedisCodec;

public abstract class CommandOutput<K, V, T> {
    protected RedisCodec<K, V> codec;
    protected T output;
    protected String error;

    public CommandOutput(RedisCodec<K, V> codec, T output) {
        this.codec  = codec;
        this.output = output;
    }

    public T get() {
        return output;
    }

    public void set(ByteBuffer bytes) {
        throw new IllegalStateException();
    }

    public void set(long integer) {
        throw new IllegalStateException();
    }

    public void setError(ByteBuffer error) {
        this.error = decodeAscii(error);
//        System.err.println("~~~~~~~~~error:" + this.error);
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean hasError() {
        return this.error != null;
    }

    public String getError() {
        return error;
    }

    public void complete(int depth) {
        // nothing to do by default
    }

    public void setOutput(T output) {
    	this.output = output;    	
    }
    
    protected String decodeAscii(ByteBuffer bytes) {
        char[] chars = new char[bytes.remaining()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) bytes.get();
        }
        return new String(chars);
    }
}
