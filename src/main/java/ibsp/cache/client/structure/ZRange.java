package ibsp.cache.client.structure;

import java.util.Set;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class ZRange extends Operate<Set<byte[]>, BinaryJedisCommands> {
	private long start; 
	private long end;

	public ZRange() {
		command = "ZRANGE";
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
	public Set<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		Set<byte[]> result = jedis.zrange(SafeEncoder.encode(getKey()), getStart(), getEnd());
        long len = 0;
        for(byte[] t : result) {
        	len += t.length;
        }
        setRespLength(len);
		return result;  
	}
}
