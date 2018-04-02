package ibsp.cache.client.structure;

import ibsp.cache.client.protocol.ByteUtil;
import redis.clients.jedis.BinaryJedisCommands;

public class Ltrim extends Operate<String,BinaryJedisCommands> {
	private long start;
	private long end;

	public Ltrim() {
		command = "LTRIM";
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
		return new byte[][]{ByteUtil.encode(String.valueOf(getStart())), ByteUtil.encode(String.valueOf(getEnd()))};
	}
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}

	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
		String result = jedis.ltrim( ByteUtil.encode(getKey()), getStart(), getEnd());
		setRespLength(result.getBytes().length);
		return result;
	}
}
