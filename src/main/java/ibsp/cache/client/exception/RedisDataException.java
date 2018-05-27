package ibsp.cache.client.exception;

public class RedisDataException extends RedisException {
	private static final long serialVersionUID = -1058901194312798845L;

	public RedisDataException(String message) {
		super(message);
	}

	public RedisDataException(Throwable cause) {
		super(cause);
	}

	public RedisDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
