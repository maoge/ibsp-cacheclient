package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Pexpire extends Operate<Long, BinaryJedisCommands> {

	private long milliseconds;
	
	public Pexpire() {
		command = "PEXPIRE";
		operateType = OperateType.WRITE;
	}
	
	public long getMilliseconds() {
		return milliseconds;
	}

	public void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getMilliseconds()).getBytes()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.pexpire(SafeEncoder.encode(getKey()), getMilliseconds());
		setRespLength(String.valueOf(result).getBytes().length);
		return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}

}
