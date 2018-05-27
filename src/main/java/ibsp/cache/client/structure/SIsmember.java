package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class SIsmember extends Operate<Boolean, BinaryJedisCommands> {

	private String member;

	public SIsmember() {
		command = "SISMEMBER";
		operateType = OperateType.READ;
	}

    public String getMember() {
		return member;
	}
    
	public void setMember(String member) {
		this.member = member;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), SafeEncoder.encode(getMember())};
	}
	
	@Override
	public Boolean doExecute(BinaryJedisCommands jedis) throws Exception {
        Boolean result = jedis.sismember(SafeEncoder.encode(getKey()), SafeEncoder.encode(getMember()));
		setRespLength(String.valueOf(result).getBytes().length);	
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+",member="+member+"]";
	}
}
