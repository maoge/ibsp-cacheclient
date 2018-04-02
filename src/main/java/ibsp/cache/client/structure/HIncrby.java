package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class HIncrby extends Operate<Long, BinaryJedisCommands> {
	private byte[] field;
	private long value;
	
	public HIncrby() {
		command = "HINCRBY";
		operateType = OperateType.WRITE;
	}
	
	public byte[] getField() {
		return field;
	}

	public void setField(byte[] field) {
		this.field = field;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getValue()).getBytes(), getField()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.hincrBy(SafeEncoder.encode(getKey()), getField(), getValue());
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
}
