package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class ZCount extends Operate<Long, BinaryJedisCommands> {
	private double max;
	private double min;

	public ZCount() {
		command = "ZCOUNT";
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
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getMin()).getBytes(), String.valueOf(getMax()).getBytes()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.zcount(SafeEncoder.encode(getKey()), getMin(), getMax());
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
}
