package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.Protocol;
import ibsp.cache.client.protocol.SafeEncoder;

public class Expire extends Operate<Long, BinaryJedisCommands> {

	private int seconds;
	
	public Expire() {
		command = "EXPIRE";
		operateType = OperateType.READ;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), Protocol.toByteArray(getSeconds())};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.expire(SafeEncoder.encode(getKey()), getSeconds());
		setRespLength(String.valueOf(result).getBytes().length);	
		return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}

}
