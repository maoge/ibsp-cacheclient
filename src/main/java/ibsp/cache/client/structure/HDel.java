package ibsp.cache.client.structure;

import ibsp.cache.client.utils.CONSTS.DataType;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class HDel extends Operate<Long, BinaryJedisCommands> {
	private String[] fields;

	public HDel() {
		command = "HDEL";
		dataType = DataType.HASH;
		operateType = OperateType.WRITE;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String... fields) {
		this.fields = fields;
	}
	
	@Override
	public byte[][] getParam() {
		return joinParameters(SafeEncoder.encode(getKey()), getFields());
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.hdel(SafeEncoder.encode(getKey()), SafeEncoder.encodeMany(getFields()));
		setRespLength(String.valueOf(result).getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+",fields="+fields+"]";
	}
}
