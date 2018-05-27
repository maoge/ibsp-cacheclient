package ibsp.cache.client.structure;

import java.util.Set;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class SMembers extends Operate<Set<byte[]>, BinaryJedisCommands> {

	public SMembers() {
		command = "SMEMBERS";
		operateType = OperateType.READ;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
		
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Set<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {    
		Set<byte[]> result = jedis.smembers(SafeEncoder.encode(getKey()));
		if(result!=null) {
	        long len = 0;
	        for(byte[] t : result) {
	            len += t.length;
	        }
	        setRespLength(len);		    
		}
		return result;
	}
	
}
