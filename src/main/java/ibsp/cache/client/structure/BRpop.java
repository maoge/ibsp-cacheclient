package ibsp.cache.client.structure;

import java.util.List;

import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.protocol.SafeEncoder;

public class BRpop extends Operate<List<byte[]>, NJedis> {
	
	private int timeout = 0;
	private byte[][] keys;
	
	public BRpop() {
		command = "BRPOP";
		operateType = OperateType.WRITE;
	}
	
    public byte[][] getKeys() {
        byte[][] bkeys = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            bkeys[i] = keys[i];
        }
        return bkeys;
    }

    public void setKeys(byte[][] keys) {
        this.keys = keys;
    }
    
    public int getTimeout() {
    	return timeout;
    }
    
    public void setTimeout(int timeout) {
    	this.timeout = timeout;
    }
	
	@Override
	public byte[][] getParam() {
		int len = keys.length;
		byte[][] params = new byte[len + 1][];
		for (int i = 0; i < len; i++) {
			params[i] = keys[i];
		}
		params[len] = SafeEncoder.encode(String.valueOf(timeout));
		return params;
	}
	
	@Override
	public List<byte[]> doExecute(NJedis jedis) throws Exception {
		List<byte[]> result = jedis.brpop(getParam());
		if (result == null) {
			setRespLength(0);
			return null;
		}
		
		int len = 0;
		for (byte[] s : result)
			len += s.length;
		
        setRespLength(len);
        return result;
	}
	
	@Override
	public String toString() {
		return "[timeout=" + timeout +", key="+getKeys()+"]";
	}

}
