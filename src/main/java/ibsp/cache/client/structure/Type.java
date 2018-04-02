package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Type extends Operate<String, BinaryJedisCommands> {

	public Type() {
		command = "TYPE";
		operateType = OperateType.READ;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
        String result = jedis.type(SafeEncoder.encode(getKey()));
        setRespLength(result.getBytes().length);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
