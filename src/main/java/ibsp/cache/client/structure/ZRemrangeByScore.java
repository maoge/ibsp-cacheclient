package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class ZRemrangeByScore extends Operate<Long, BinaryJedisCommands> {

	private long start;
	private long end;

	public ZRemrangeByScore() {
		command = "ZREMRANGEBYSCORE";
		operateType = OperateType.WRITE;
	}

    public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getStart()).getBytes(), String.valueOf(getEnd()).getBytes()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.zremrangeByScore(SafeEncoder.encode(getKey()), getStart(), getEnd());
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
