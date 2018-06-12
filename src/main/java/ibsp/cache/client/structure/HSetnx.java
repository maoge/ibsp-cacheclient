package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.common.utils.CONSTS.DataType;

public class HSetnx extends Operate<Long, BinaryJedisCommands> {

	private String field;
	private String value;

	public HSetnx() {
		command = "HSETNX";
		dataType = DataType.HASH;
		operateType = OperateType.WRITE;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

    public void setValue(String value) {
		this.value = value;
	}
    
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), SafeEncoder.encode(getField()), SafeEncoder.encode(getValue())};
	}

	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.hsetnx(SafeEncoder.encode(getKey()), SafeEncoder.encode(getField()), SafeEncoder.encode(getValue()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+",field="+field+",value="+value+"]";
	}
}
