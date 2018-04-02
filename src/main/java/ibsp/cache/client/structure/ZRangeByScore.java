package ibsp.cache.client.structure;

import java.util.Set;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class ZRangeByScore extends Operate<Set<byte[]>, BinaryJedisCommands> {
	private double max;
	private double min;
	private int offset;
	private int count;
	private boolean offsetFlag=false;
	
	public ZRangeByScore() {
		command = "ZRANGEBYSCORE";
		operateType = OperateType.READ;
	}
	
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public boolean isOffsetFlag() {
		return offsetFlag;
	}

	public void setOffsetFlag(boolean offsetFlag) {
		this.offsetFlag = offsetFlag;
	}

	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getMin()).getBytes(), String.valueOf(getMax()).getBytes()};
	}
	
	@Override
	public Set<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		Set<byte[]> result = null;
		if(isOffsetFlag()){
			result = jedis.zrangeByScore(SafeEncoder.encode(getKey()), getMin(), getMax(),getOffset(),getCount());
		}else{
			result = jedis.zrangeByScore(SafeEncoder.encode(getKey()), getMin(), getMax());
		}
        long len = 0;
        for(byte[] t : result) {
        	len += t.length;
        }
        setRespLength(len);
		return result;
	}
	
}
