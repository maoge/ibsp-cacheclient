package ibsp.cache.client.structure;

import java.util.List;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.ByteUtil;
import ibsp.common.utils.CONSTS.DataType;

public class HMget4Bit extends Operate<List<byte[]>,BinaryJedisCommands> {
	private String[] fields;
	
	public HMget4Bit() {
		command = "HMGET";
		dataType = DataType.HASH;
		operateType = OperateType.READ;
	}

	public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    @Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
				
	@Override
	public byte[][] getParam() {
		int i = 0;
		byte[][] bFields = new byte[getFields().length][];
		for(String field : getFields()) {
			bFields[i++] = ByteUtil.encode( field );
		}
		return bFields;
	}
	

	@Override
	public List<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		List<byte[]> result = jedis.hmget(ByteUtil.encode(getKey()), getParam());
		if(result!=null) {
	        long len = 0;
	        for(byte[] b : result) {
	            len += b.length;
	        }
	        setRespLength(len);		    
		}
        return result;
	}
}
