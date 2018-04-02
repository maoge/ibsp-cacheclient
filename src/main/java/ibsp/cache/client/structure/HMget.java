package ibsp.cache.client.structure;

import java.util.List;

import redis.clients.util.SafeEncoder;
import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.utils.CONSTS.DataType;

public class HMget extends Operate<List<String>, NJedis> {
	private String[] fields;
	
	public HMget() {
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
	public List<String> doExecute(NJedis jedis) throws Exception {
		List<String> result = jedis.hmget(getKey(), getFields());
		if(result!=null) {
	        long len = 0;
	        for(String s : result) {
	            len += s.length();
	        }
	        setRespLength(len);		    
		}
        return result;
	}
}
