package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.util.SafeEncoder;

public class GetRange extends Operate<byte[], BinaryJedisCommands> {
	private long startOffset;
	private long endOffset;	

	public GetRange() {
		command = "GETRANGE";
		operateType = OperateType.READ;
	}
	
    public long getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(long startOffset) {
		this.startOffset = startOffset;
	}

	public long getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(long endOffset) {
		this.endOffset = endOffset;
	}
	
	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(String.valueOf(getStartOffset())), SafeEncoder.encode(String.valueOf(getEndOffset()))};
	}
	
	@Override
	public byte[] doExecute(BinaryJedisCommands jedis) throws Exception {
		byte[] result = jedis.getrange(SafeEncoder.encode(getKey()), getStartOffset(), getEndOffset());
        if(result!=null) setRespLength(result.length);      
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
