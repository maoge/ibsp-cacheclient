package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Lpushx extends Operate<Long, BinaryJedisCommands> {
    private String[] values;
	
	public Lpushx() {
		command = "LPUSHX";
		operateType = OperateType.WRITE;
	}

	public void setValues(final String... values) {
		this.values = values;
	}
   
	public String[] getValues() {
		return this.values;
	}
	
	private byte[][] getBinValues() {
	    final byte[][] bvalues = new byte[values.length][];
	    for (int i = 0; i < values.length; i++) {
	    	bvalues[i] = SafeEncoder.encode(values[i]);
	    }
	    return bvalues;
	}
	
	@Override
	public byte[][] getParam() {
		return concatArray(getBinValues(), new byte[][]{SafeEncoder.encode(getKey())});		
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.lpushx(SafeEncoder.encode(getKey()), getBinValues());
		setRespLength(String.valueOf(result).getBytes().length);	
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
