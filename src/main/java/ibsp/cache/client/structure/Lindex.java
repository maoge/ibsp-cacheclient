package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Lindex extends Operate<byte[], BinaryJedisCommands> {
	private long index;

	public Lindex() {
		command = "LINDEX";
		operateType = OperateType.READ;
	}
	
	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(String.valueOf(getIndex()))};
	}
	
	@Override
	public byte[] doExecute(BinaryJedisCommands jedis) throws Exception {
		byte[] result = jedis.lindex(SafeEncoder.encode(getKey()), getIndex());
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
