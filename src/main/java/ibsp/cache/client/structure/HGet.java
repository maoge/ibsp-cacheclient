package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.common.utils.CONSTS.DataType;

public class HGet extends Operate <String, BinaryJedisCommands> {

	private String field;
	
	public HGet() {
		command = "HGET";
		dataType = DataType.HASH;
		operateType = OperateType.READ;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+",field="+field+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), SafeEncoder.encode(getField())};
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
        byte[] result = jedis.hget(SafeEncoder.encode(getKey()), SafeEncoder.encode(getField()));
        if(result!=null) {
           setRespLength(result.length);
           return SafeEncoder.encode(result);
        }
        return null;
	}
	
}
