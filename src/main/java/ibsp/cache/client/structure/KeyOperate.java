package ibsp.cache.client.structure;

import redis.clients.jedis.BinaryJedisCommands;

public class KeyOperate extends Operate<String, BinaryJedisCommands> {
	private Object attr;
	private boolean hasAttr;
	
	public KeyOperate(){
		hasAttr = false;
		command = "";
		operateType = OperateType.READ;
	}
	
	public Object getAttr() {
		return attr;
	}

	public boolean isHasAttr() {
		return hasAttr;
	}

	public void setAttr(Object attr) {
		hasAttr = true;
		this.attr = attr;
	}

	@Override
	public byte[][] getParam() {
		return null;
		//return new byte[][]{SafeEncoder.encode(getKey()), SafeEncoder.encode(getField())};
	}
	
	@Override
	public String doExecute(BinaryJedisCommands jedis) throws Exception {
        return null;
	}
	
	public String toString(){
		return "[key=" + key + "]";
	}
}
