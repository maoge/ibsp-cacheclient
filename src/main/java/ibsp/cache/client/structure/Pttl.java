package ibsp.cache.client.structure;

import ibsp.cache.client.core.NBinaryJedis;
import ibsp.cache.client.protocol.SafeEncoder;

public class Pttl extends Operate<Long, NBinaryJedis> {

	public Pttl() {
		command = "PTTL";
		operateType = OperateType.READ;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Long doExecute(NBinaryJedis jedis) throws Exception {
		Long result = jedis.pttl(SafeEncoder.encode(getKey()));
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
