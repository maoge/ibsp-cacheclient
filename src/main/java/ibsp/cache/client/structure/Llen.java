package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Llen extends Operate<Long, BinaryJedisCommands> {

	public Llen() {
		command = "LLEN";
		operateType = OperateType.READ;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.llen(SafeEncoder.encode(getKey()));
		setRespLength(String.valueOf(result).getBytes().length);	
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
