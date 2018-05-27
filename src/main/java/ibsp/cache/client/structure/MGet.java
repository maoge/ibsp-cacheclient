package ibsp.cache.client.structure;

import java.util.List;

import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.protocol.SafeEncoder;

public class MGet extends Operate<List<byte[]>, NJedis> {
	private String[] keys;

	public MGet() {
		command = "MGET";
		operateType = OperateType.READ;
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
    
    @Override
	public byte[][] getParam() {        
		return getKeys();
	}
	
	@Override
	public List<byte[]> doExecute(NJedis jedis) throws Exception {
	    List<byte[]> result = jedis.mget(getKeys());
	    long len = 0;
	    for(byte[] t : result) {
	        len += t.length;
	    }
        setRespLength(len);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKeys()+"]";
	}
}
