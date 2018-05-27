package ibsp.cache.client.structure;

import java.util.Set;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.protocol.Tuple;

public class ZRangeByScoreWithScores extends Operate<Set<Tuple>, BinaryJedisCommands> {
	private double max;
	private double min;
	private int offset;
	private int count;

	private boolean offsetFlag=false;
	
	public ZRangeByScoreWithScores() {
		command = "ZRANGEBYSCOREWITHSCORES";
		operateType = OperateType.READ;
	}
	
	public boolean isOffsetFlag() {
		return offsetFlag;
	}

	public void setOffsetFlag(boolean offsetFlag) {
		this.offsetFlag = offsetFlag;
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
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getMin()).getBytes(), String.valueOf(getMax()).getBytes()};
	}
	
	@Override
	public Set<Tuple> doExecute(BinaryJedisCommands jedis) throws Exception {
		Set<Tuple> result ;
		if(isOffsetFlag()){
			result = jedis.zrangeByScoreWithScores(SafeEncoder.encode(getKey()), getMin(), getMax(),getOffset(),getCount());
		}else{
			result = jedis.zrangeByScoreWithScores(SafeEncoder.encode(getKey()), getMin(), getMax());
		}
        long len = 0;
        for(Tuple t : result) {
        	len += t.getBinaryElement().length;
        }
        setRespLength(len);
		return result;
	}

}
