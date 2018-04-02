package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Lpop extends Operate<byte[], BinaryJedisCommands> {

	public Lpop() {
		command = "LPOP";
		operateType = OperateType.READ;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public byte[] doExecute(BinaryJedisCommands jedis) throws Exception {
		byte[] result = jedis.lpop(SafeEncoder.encode(getKey()));
		setRespLength(result.length);	
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
