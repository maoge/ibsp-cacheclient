package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class SCard extends Operate<Long, BinaryJedisCommands> {

	public SCard() {
		command = "SCARD";
		operateType = OperateType.READ;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.scard(SafeEncoder.encode(getKey()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
    @Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
