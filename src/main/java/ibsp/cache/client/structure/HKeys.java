package ibsp.cache.client.structure;

import java.util.Set;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class HKeys extends Operate<Set<byte[]>, BinaryJedisCommands> {

	public HKeys() {
		command = "HKEYS";
		operateType = OperateType.WRITE;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public Set<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		Set<byte[]> result = jedis.hkeys(SafeEncoder.encode(getKey()));
		if(result!=null) {		    
    		long respLength = 0L;
    		for(byte[] ret : result) {
    			respLength += ret.length;
    		}
    		setRespLength(respLength);
		}
		return result;
	}
	
    @Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
