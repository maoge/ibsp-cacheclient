package ibsp.cache.client.structure;

import ibsp.cache.client.command.BinaryClient.LIST_POSITION;
import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.protocol.SafeEncoder;

public class Linsert extends Operate<Long, BinaryJedisCommands> {
	private byte[] pivot;
    private byte[] value;
    private boolean before;
    
	public Linsert() {
		command = "LINSERT";
		operateType = OperateType.WRITE;
	}
	

	public void setBefore(boolean before) {
		this.before = before;
	}
	
	public LIST_POSITION getWhere() {
		return  before ? LIST_POSITION.BEFORE : LIST_POSITION.AFTER;
	}

	public byte[] getPivot() {
		return pivot;
	}

	public void setPivot(byte[] pivot) {
		this.pivot = pivot;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{getWhere().raw, getPivot(), getValue()};
	}
	
	@Override
	public Long doExecute(BinaryJedisCommands jedis) throws Exception {
		Long result = jedis.linsert(SafeEncoder.encode(getKey()), getWhere(), getPivot(), getValue());
		setRespLength(String.valueOf(result).getBytes().length);	
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[key=").append(getKey()).append("]");
		return buf.toString();
	}
}
