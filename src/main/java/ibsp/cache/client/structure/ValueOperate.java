package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;

public class ValueOperate extends Operate<String, BinaryJedisCommands> {
	private String value;
	private byte[] byteValue;
	private Object attr;
	private boolean hasAttr;
	
	public ValueOperate(){
		hasAttr = false;
		command = "";
		operateType = OperateType.READ;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public byte[] getByteValue() {
		return byteValue;
	}

	public void setByteValue(byte[] byteValue) {
		isByteValue = true;
		this.byteValue = byteValue;
	}

	public Object getAttr() {
		return attr;
	}

	public void setAttr(Object attr) {
		hasAttr = true;
		this.attr = attr;
	}

	public boolean isHasAttr() {
		return hasAttr;
	}

	@Override
	public byte[][] getParam() {
		return null;
		//return new byte[][]{SafeEncoder.encode(getKey()), SafeEncoder.encode(getField())};
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
        return null;
	}
	
	public String toString(){
		StringBuilder buf = new StringBuilder();
		
		buf.append("[key=").append(key).append(",value=").append(isByteValue() ? new String(byteValue) : value).append("]");
		
		return buf.toString();
	}
}
