package ibsp.cache.client.structure;

import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.protocol.SafeEncoder;

public class MSet extends Operate<String, NJedis> {
	private String[] keysvalues;

	public MSet() {
		command = "MSET";
		operateType = OperateType.WRITE;
	}
	
	public String[] getKeysvalues() {
        String[] gkeyvalues = new String[keysvalues.length];
        for (int i = 0; i < keysvalues.length; i++) {
             if(i==0 || i%2==0) {
                 gkeyvalues[i] = getGroupId()==null ? keysvalues[i] : getGroupId()+SPLIT+keysvalues[i];
             } else {
                 gkeyvalues[i] = keysvalues[i];
             }
        }
        return keysvalues;
    }

    public void setKeysvalues(String[] keysvalues) {
        this.keysvalues = keysvalues;
    }

    @Override
	public byte[][] getParam() {        
		return SafeEncoder.encodeMany(getKeysvalues());
	}
	
	@Override
	public String doExecute(NJedis jedis) throws Exception {
        String result = jedis.mset(getKeysvalues());
        setRespLength(result.getBytes().length);
        return result;
	}
	
	@Override
	public String toString() {
		return "[key="+getKeysvalues()+"]";
	}
}
