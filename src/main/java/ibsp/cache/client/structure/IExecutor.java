package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;

public interface IExecutor<T, E extends BinaryJedisCommands> {
	T doExecute(E jedis) throws Exception;
}
