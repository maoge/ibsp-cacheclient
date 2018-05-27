package ibsp.cache.client.exception;

public class RedisException extends RuntimeException {
	private static final long serialVersionUID = 512923767774858376L;

	public RedisException(String message) {
		super(message);
	}

	public RedisException(Throwable e) {
		super(e);
	}

	public RedisException(String message, Throwable cause) {
		super(message, cause);
	}
}
