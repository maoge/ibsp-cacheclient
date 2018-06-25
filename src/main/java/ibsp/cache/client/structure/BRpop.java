package ibsp.cache.client.structure;

import java.util.List;

import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.protocol.SafeEncoder;

public class BRpop extends Operate<List<byte[]>, NJedis> {
	
	private int timeout = 0;
	private String[] keys;
	
	public BRpop() {
		command = "BRPOP";
		operateType = OperateType.WRITE;
	}
	
    public byte[][] getKeys() {
        byte[][] bkeys = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            bkeys[i] = SafeEncoder.encode(keys[i]);
        }
        return bkeys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }
    
    public int getTimeout() {
    	return timeout;
    }
    
    public void setTimeout(int timeout) {
    	this.timeout = timeout;
    }
	
//	@Override
//	public byte[][] getParam() {
//		byte[][] params = new byte[keys.length + 1][];
//		params[0] = SafeEncoder.encode(String.valueOf(timeout));
//		for (int i = 0; i < keys.length; i++) {
//			params[i] = SafeEncoder.encode(keys[i]);
//		}
//		
//		return params;
//	}
    
	@Override
	public byte[][] getParam() {
		return null;
	}
	
	@Override
	public List<byte[]> doExecute(NJedis jedis) throws Exception {
		List<byte[]> result = jedis.brpop(timeout, getKeys());
		long len = 0;
	    for(byte[] t : result) {
	        len += t.length;
	    }
        setRespLength(len);
        return result;
	}
	
	@Override
	public String toString() {
		return "[timeout=" + timeout +", key="+getKeys()+"]";
	}

}
