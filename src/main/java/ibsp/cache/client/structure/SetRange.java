package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class SetRange extends Operate<Long, BinaryJedisCommands> {
	private long offset;
	private byte[] value;	

	public SetRange() {
		command = "SETRANGE";
		operateType = OperateType.WRITE;
	}
	
	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(String.valueOf(getOffset())), getValue()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.setrange(SafeEncoder.encode(getKey()), getOffset(), getValue());
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
