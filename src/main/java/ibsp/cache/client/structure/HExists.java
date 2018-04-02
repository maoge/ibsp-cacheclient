package ibsp.cache.client.structure;

import ibsp.cache.client.utils.CONSTS.DataType;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class HExists extends Operate<Boolean, BinaryJedisCommands> {

	private String field;

	public HExists() {
		command = "HEXISTS";
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
	public Boolean doExecute(BinaryJedisCommands jedis) throws Exception {
        boolean result = jedis.hexists(SafeEncoder.encode(getKey()), SafeEncoder.encode(getField()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
}
