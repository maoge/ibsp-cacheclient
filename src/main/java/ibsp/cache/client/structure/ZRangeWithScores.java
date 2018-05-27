package ibsp.cache.client.structure;

import java.util.Set;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.cache.client.protocol.Tuple;

public class ZRangeWithScores extends Operate<Set<Tuple>, BinaryJedisCommands> {
	private long start; 
	private long end;

	public ZRangeWithScores() {
		command = "ZRANGEWITHSCORES";
		operateType = OperateType.READ;
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
	public String toString() {
		return "[key="+getKey()+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getStart()).getBytes(), String.valueOf(getEnd()).getBytes()};
	}
	
	@Override
	public Set<Tuple> doExecute(BinaryJedisCommands jedis) throws Exception {
		Set<Tuple> result = jedis.zrangeWithScores(SafeEncoder.encode(getKey()), getStart(), getEnd());
        long len = 0;
        for(Tuple t : result) {
        	len += t.getBinaryElement().length;
        }
        setRespLength(len);
		return result;
	}
	
}
