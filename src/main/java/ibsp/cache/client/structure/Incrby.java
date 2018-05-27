package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Incrby extends Operate<Long, BinaryJedisCommands> {
	private long integer;

	public Incrby() {
		command = "INCRBY";
		operateType = OperateType.WRITE;
	}
	
    public long getInteger() {
		return integer;
	}

	public void setInteger(long integer) {
		this.integer = integer;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(integer).getBytes()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.incrBy(SafeEncoder.encode(getKey()), getInteger());
		setRespLength(String.valueOf(result).getBytes().length);	
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append(",value=").append(getInteger()).append("]");
		return buf.toString();
	}
}
