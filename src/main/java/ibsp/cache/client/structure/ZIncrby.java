package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class ZIncrby extends Operate<Double, BinaryJedisCommands> {
	private double score;
	private String member;

	public ZIncrby() {
		command = "ZINCRBY";
		operateType = OperateType.READ;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getMember() {
		return member;
	}
	public void setMember(String member) {
		this.member = member;
	}
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getScore()).getBytes(), SafeEncoder.encode(getMember())};
	}
	
	@Override
	public Double doExecute(BinaryJedisCommands jedis) throws Exception {
        Double result = jedis.zincrby(SafeEncoder.encode(getKey()), getScore(), SafeEncoder.encode(getMember()));
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
}
