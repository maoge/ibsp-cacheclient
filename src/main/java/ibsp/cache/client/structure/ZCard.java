package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class ZCard extends Operate<Long, BinaryJedisCommands> {
	public ZCard() {
		command = "ZCARD";
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
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.zcard(SafeEncoder.encode(getKey()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}

}
