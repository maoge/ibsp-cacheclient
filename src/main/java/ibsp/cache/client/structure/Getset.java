package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Getset extends Operate<byte[], BinaryJedisCommands> {
	private String value;
	private byte[] byteValue;

	public Getset() {
		command = "GETSET";
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
	public byte[] doExecute(BinaryJedisCommands jedis) throws Exception {
		byte[] result = jedis.getSet(SafeEncoder.encode(getKey()), getByteValue());
        if(result!=null) setRespLength(result.length);      
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append(",value=").append(isByteValue ? new String(byteValue) : value).append("]");
		return buf.toString();
	}
}
