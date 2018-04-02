package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;

public class ValuesOperate extends Operate<String, BinaryJedisCommands> {
	private Object value;
	private Object byteValue;
	
	public ValuesOperate(){
		command = "";
		operateType = OperateType.READ;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getByteValue() {
		return byteValue;
	}

	public void setByteValue(Object byteValue) {
		isByteValue = true;
		this.byteValue = byteValue;
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
		return "[key=" + key + "]";
	}
}
