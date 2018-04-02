package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Rpop extends Operate<byte[], BinaryJedisCommands> {

	public Rpop() {
		command = "RPOP";
		operateType = OperateType.WRITE;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(String.valueOf(getKey()))};
	}
	
	@Override
	public byte[] doExecute(BinaryJedisCommands jedis) throws Exception {
		byte[] result = jedis.rpop(SafeEncoder.encode(getKey()));
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
