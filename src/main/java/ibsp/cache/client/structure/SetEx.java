package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class SetEx extends Operate<String, BinaryJedisCommands> {
	private String value;
	private int  seconds;
	private byte[] byteValue;

	public SetEx() {
		command = "SETEX";
		operateType = OperateType.WRITE;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
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
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
		String result = jedis.setex(SafeEncoder.encode(getKey()), getSeconds(), getByteValue());
		setRespLength(result.getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append(",value=").append(isByteValue ? new String(byteValue) : value).append("]");
		return buf.toString();
	}
}
