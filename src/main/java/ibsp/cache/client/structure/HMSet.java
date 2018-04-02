package ibsp.cache.client.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class HMSet extends Operate<String, BinaryJedisCommands> {
	private Map<String ,String> hash;

	public HMSet() {
		command = "HMSET";
		operateType = OperateType.WRITE;
	}

    public Map<String, String> getHash() {
		return hash;
	}
    
	public void setHash(Map<String, String> hash) {
		this.hash = hash;
	}
	
	@Override
	public byte[][] getParam() {
        List<byte[]> params = new ArrayList<byte[]>();
        params.add(SafeEncoder.encode(getKey()));
	    Map<byte[], byte[]> bhash = new HashMap<byte[], byte[]>(getHash().size());
	    for (Map.Entry<String, String> entry : getHash().entrySet()) {
	         params.add(SafeEncoder.encode(entry.getKey()));
	         params.add(SafeEncoder.encode(entry.getValue()));	        
	         bhash.put(SafeEncoder.encode(entry.getKey()), SafeEncoder.encode(entry.getValue()));
	    }
		return params.toArray(new byte[params.size()][]);
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
	    Map<byte[], byte[]> bhash = new HashMap<byte[], byte[]>(getHash().size());
	    for (Map.Entry<String, String> entry : getHash().entrySet()) {
	         bhash.put(SafeEncoder.encode(entry.getKey()), SafeEncoder.encode(entry.getValue()));
	    }
        String result = jedis.hmset(SafeEncoder.encode(getKey()), bhash);
        setRespLength(result.getBytes().length);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
