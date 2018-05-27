package ibsp.cache.client.structure;

import java.util.List;

import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.utils.CONSTS.DataType;

public class HMget4Str extends Operate<List<byte[]>, NJedis> {
	private String[] fields;
	
	public HMget4Str() {
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
		return joinParameters(SafeEncoder.encode(getKey()), getFields());
	}
	
	@Override
	public List<byte[]> doExecute(NJedis jedis) throws Exception {
		List<byte[]> result = jedis.hmgetBytes(getKey(), getFields());
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
