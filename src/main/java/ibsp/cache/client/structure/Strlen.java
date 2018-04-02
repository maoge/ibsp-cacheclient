package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Strlen extends Operate<Long, BinaryJedisCommands> {

	public Strlen() {
		command = "STRLEN";
		operateType = OperateType.READ;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.strlen(SafeEncoder.encode(getKey()));
		setRespLength(String.valueOf(result).getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}

}
