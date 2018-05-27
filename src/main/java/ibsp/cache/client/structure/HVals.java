package ibsp.cache.client.structure;

import java.util.List;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

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
