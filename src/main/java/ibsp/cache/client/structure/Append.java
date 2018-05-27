package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Append extends Operate<Long, BinaryJedisCommands> {
	private String value;
	private byte[] byteValue;

	public Append() {
		command = "APPEND";
		operateType = OperateType.WRITE;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public byte[] getByteValue() {    	
		return byteValue !=null ? byteValue : SafeEncoder.encode(value);
	}
    
	public void setByteValue(byte[] byteValue) {
		isByteValue = true;
		this.byteValue = byteValue;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), getByteValue()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.append(SafeEncoder.encode(getKey()), getByteValue());
		setRespLength(String.valueOf(result).getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append(",value=").append(isByteValue ? new String(byteValue) : value).append("]");
		return buf.toString();
	}
}
