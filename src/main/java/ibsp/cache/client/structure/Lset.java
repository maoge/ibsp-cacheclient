package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Lset extends Operate<String, BinaryJedisCommands> {
	private long index;
	private byte[] value;	

	public Lset() {
		command = "LSET";
		operateType = OperateType.WRITE;
	}
	
	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(String.valueOf(getIndex())), getValue()};
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
		String result = jedis.lset(SafeEncoder.encode(getKey()), getIndex(), getValue());
		setRespLength(result.getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
