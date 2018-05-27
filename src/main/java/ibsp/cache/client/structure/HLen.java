package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class HLen extends Operate<Long, BinaryJedisCommands> {

	public HLen() {
		command = "HLEN";
		operateType = OperateType.READ;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.hlen(SafeEncoder.encode(getKey()));
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
