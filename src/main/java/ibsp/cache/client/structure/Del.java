package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Del extends Operate<Long, BinaryJedisCommands> {

	public Del() {
		command = "DEL";
		operateType = OperateType.WRITE;
	}
	
    private String[] keys;
	
	public void setKeys(final String... keys) {
		this.keys = keys;
	}
   
	public String[] getKeys() {
		return this.keys;
	}
	
	private byte[][] getBinKeys() {
	    byte[][] bkeys = new byte[keys.length][];
	    for (int i = 0; i < keys.length; i++) {
	      bkeys[i] = SafeEncoder.encode(keys[i]);
	    }
	    return bkeys;
	}
	
	@Override
	public byte[][] getParam() {
		if(key!=null) {
		  return new byte[][]{SafeEncoder.encode(getKey())};
		} else if(keys!=null) {
		  return getBinKeys();
		}
		return null;
	}
		
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = 0L;
		if(getKey()!=null) {
			result = jedis.del(SafeEncoder.encode(getKey()));			
		} else if(getBinKeys()!=null) {
		    for(byte[] key : getBinKeys()) {
	            result = jedis.del(key);           		        
		    }
//			result = ((NBinaryJedis)jedis).del(getBinKeys()); //接入机不支持多 key 操作	
		}
		setRespLength(String.valueOf(result).getBytes().length);		
		return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
