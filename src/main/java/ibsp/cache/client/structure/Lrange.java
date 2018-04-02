package ibsp.cache.client.structure;

import java.util.List;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class Lrange extends Operate<List<byte[]>, BinaryJedisCommands> {
	private long start;
	private long end;	

	public Lrange() {
		command = "LRANGE";
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
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(String.valueOf(getStart())), SafeEncoder.encode(String.valueOf(getEnd()))};
	}
	
	@Override
	public List<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		List<byte[]> result = jedis.lrange(SafeEncoder.encode(getKey()), getStart(), getEnd());
		long len = 0;
		for(byte[] b : result) {
			len += b.length;
		}
		setRespLength(len);	
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
