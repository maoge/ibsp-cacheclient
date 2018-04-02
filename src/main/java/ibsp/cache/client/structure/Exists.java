package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Exists extends Operate<Boolean, BinaryJedisCommands> {

	public Exists() {
		command = "EXISTS";
		operateType = OperateType.READ;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Boolean doExecute(BinaryJedisCommands jedis) throws Exception {
		boolean result = jedis.exists(SafeEncoder.encode(getKey()));
		setRespLength(String.valueOf(result).getBytes().length);	
		return result;
	}
}
