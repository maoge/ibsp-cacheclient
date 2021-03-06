package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Rpushx extends Operate<Long, BinaryJedisCommands> {
    private String[] values;
	
	public Rpushx() {
		command = "RPUSHX";
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
		Long result = jedis.rpushx(SafeEncoder.encode(getKey()), getBinValues());
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
