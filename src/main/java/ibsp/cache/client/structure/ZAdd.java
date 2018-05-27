package ibsp.cache.client.structure;

import java.util.HashMap;
import java.util.Map;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class ZAdd extends Operate<Long, BinaryJedisCommands> {

	private String member;
	private double score;
	private Map<String, Double> scoreMembers;

	public ZAdd() {
		command = "ZADD";
		operateType = OperateType.WRITE;
	}

    public String getMember() {
		return member;
	}
	public void setMember(String member) {
		this.member = member;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public Map<String, Double> getScoreMembers() {
		return scoreMembers;
	}
	public void setScoreMembers(Map<String, Double> scoreMembers) {
		this.scoreMembers = scoreMembers;
	}
	
	@Override
	public byte[][] getParam() {
		if(getScoreMembers()!=null) {
		   int i = 0;
		   byte[][] _tmp = new byte[getScoreMembers().size() * 2][];
           for(Map.Entry<String, Double> map : getScoreMembers().entrySet()) {
        	   _tmp[i++] = map.getKey().getBytes();
        	   _tmp[i++] = String.valueOf(map.getValue()).getBytes();
           }
           return concatArray(new byte[][]{SafeEncoder.encode(getKey())}, _tmp) ;
		}
		return new byte[][]{SafeEncoder.encode(getKey()), String.valueOf(getScore()).getBytes(), SafeEncoder.encode(getMember())};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = 0l;
		if(getScoreMembers()!=null) {
			   Map<byte[], Double> scoreMembers = new HashMap<byte[], Double>();
	           for(Map.Entry<String, Double> map : getScoreMembers().entrySet()) {
	        	   scoreMembers.put(SafeEncoder.encode(map.getKey()),map.getValue());    			   
	           }
	           result = jedis.zadd(SafeEncoder.encode(getKey()), scoreMembers);
		} else {
			result = jedis.zadd(SafeEncoder.encode(getKey()), getScore(), SafeEncoder.encode(getMember()));	
		}  
        setRespLength(String.valueOf(result).getBytes().length);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+",value="+member+"]";
	}
}
