package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.common.utils.CONSTS.DataType;

public class HGet4Bit extends Operate <byte[], BinaryJedisCommands> {

	private byte[] field;
	
	public HGet4Bit() {
		command = "HGET";
		dataType = DataType.HASH;
		operateType = OperateType.READ;
	}

	public byte[] getField() {
        return field;
    }

    public void setField(byte[] field) {
        this.field = field;
    }

    @Override
	public String toString() {
		return "[key="+getKey()+",field="+field+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), getField()};
	}
	
	@Override
	public byte[] doExecute(BinaryJedisCommands jedis) throws Exception {
        byte[] result = jedis.hget(SafeEncoder.encode(getKey()), getField());
        if(result!=null) {
            setRespLength(result.length);
            return result;
         }
         return null;
	}
	
}
