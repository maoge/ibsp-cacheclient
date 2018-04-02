package ibsp.cache.client.structure;

import java.util.List;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class HVals extends Operate<List<byte[]>, BinaryJedisCommands> {

	public HVals() {
		command = "HVALS";
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
	public List<byte[]> doExecute(BinaryJedisCommands jedis) throws Exception {
		List<byte[]> result = (List<byte[]>) jedis.hvals(SafeEncoder.encode(getKey()));
		long len = 0;
		for(byte[] b : result) {
			len += b.length;
		}
		setRespLength(len);		
		return result;
	}
	
}
