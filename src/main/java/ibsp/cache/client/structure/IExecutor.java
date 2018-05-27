package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;

public interface IExecutor<T, E extends BinaryJedisCommands> {
	T doExecute(E jedis) throws Exception;
}
