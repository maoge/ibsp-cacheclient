package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class SRem extends Operate<Long, BinaryJedisCommands> {

	private String[] members;

	public SRem() {
		command = "SREM";
		operateType = OperateType.WRITE;
	}

	public String[] getMembers() {
		return members;
	}

	public void setMembers(String[] members) {
		this.members = members;
	}

	@Override
	public byte[][] getParam() {
		return SafeEncoder.encodeMany(getMembers());
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
        Long result = jedis.srem(SafeEncoder.encode(getKey()), SafeEncoder.encodeMany(getMembers()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
    @Override
	public String toString() {
		return "[key="+getKey()+",members="+members+"]";
	}
}
