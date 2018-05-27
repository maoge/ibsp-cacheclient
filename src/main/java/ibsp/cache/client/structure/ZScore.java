package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class ZScore extends Operate<Double, BinaryJedisCommands> {

	private String member;

	public ZScore() {
		command = "ZSCORE";
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
	public Double doExecute(BinaryJedisCommands jedis) throws Exception {
        Double result = jedis.zscore(SafeEncoder.encode(getKey()), SafeEncoder.encode(getMember()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
    @Override
	public String toString() {
		return "[key="+getKey()+",members="+member+"]";
	}
}
