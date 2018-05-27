package ibsp.cache.client.exception;

public class RedisConnectionException extends RedisException {
	private static final long serialVersionUID = 2227943259813799609L;

	public RedisConnectionException(String message) {
		super(message);
	}

	public RedisConnectionException(Throwable cause) {
		super(cause);
	}

	public RedisConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
