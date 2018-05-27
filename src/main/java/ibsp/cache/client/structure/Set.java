package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Set extends Operate<String, BinaryJedisCommands> {
	private String value;
	private byte[] byteValue;
	private int second = 0;
	private boolean nx = false;
	private boolean xx = false;


	public Set() {
		command = "SET";
		operateType = OperateType.WRITE;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public byte[] getByteValue() {    	
		return byteValue !=null ? byteValue : SafeEncoder.encode(value);
	}
    
	public void setByteValue(byte[] byteValue) {
		isByteValue = true;
		this.byteValue = byteValue;
	}
	
	@Override
	public byte[][] getParam() {
		int count = 0;
		if (second>0) count += 2;
		if (nx) count++;
		if (xx) count++;
		byte[][] result = new byte[count+2][];
		
		result[0] = SafeEncoder.encode(getKey());
		result[1] = getByteValue();
		int point = 2;
		
		if (nx) {
			result[point++] = getNX();
		}
		if (xx) {
			result[point++] = getXX();
		}
		if (second>0) {
			result[point++] = getEX();
			result[point++] = SafeEncoder.encode(""+getSecond());
		}
		
		return result;
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
		String result = null;
		
		if(nx && second > 0) {
			result = jedis.set(SafeEncoder.encode(getKey()), getByteValue(),
					getNX(),  getEX(), getSecond());
		} else if (xx && second > 0) {
			result = jedis.set(SafeEncoder.encode(getKey()), getByteValue(),
					getXX(),  getEX(), getSecond());
		} else if (nx && second==0) {
			result = jedis.setnx(SafeEncoder.encode(getKey()), getByteValue())==1?
					"OK":"not OK";
		} else if (!nx && second>0) {
			result = jedis.setex(SafeEncoder.encode(getKey()), getSecond(), getByteValue());
		} else {
			result = jedis.set(SafeEncoder.encode(getKey()), getByteValue());
		}
		
		if (result==null) result = "not OK";
		setRespLength(result.getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append(",value=").append(isByteValue ? new String(byteValue) : value).append("]");
		return buf.toString();
	}
	
	public int getSecond() {
		return second;
	}
	
	public void setSecond(int second) {
		this.second = second;
	}
	
	public byte[] getEX() {
		return second>0 ? SafeEncoder.encode("EX") : null;
	}
	
	public byte[] getNX() {
		return nx ? SafeEncoder.encode("NX") : null;
	}
	
	public void setNX(boolean nx) {
		this.nx = nx;
	}
	
	public byte[] getXX() {
		return xx ? SafeEncoder.encode("XX") : null;
	}
	
	public void setXX(boolean xx) {
		this.xx = xx;
	}
}
