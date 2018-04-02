package ibsp.cache.client.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.ScanParams;
import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.protocol.ByteUtil;
import ibsp.cache.client.protocol.Keyword;
import ibsp.cache.client.protocol.ScanResult;
import ibsp.cache.client.utils.CONSTS.DataType;
import redis.clients.util.SafeEncoder;

public class HScan extends Operate<ScanResult<Map<byte[], byte[]>>, NJedis> {
    private ScanParams params = new ScanParams();
    private byte[] cursor;
	
	public HScan() {
		command = "HSCAN";
		dataType = DataType.HASH;
		operateType = OperateType.READ;
	}
	
	public ScanParams getParams() {
		return params;
	}

	public void setParams(ibsp.cache.client.protocol.ScanParams params) {
		Collection<byte[]> paramsList = params.getParams();
		int state = 0;
		for (byte[] param : paramsList) {
			String paramString = ByteUtil.encode(param);
			
			if (state > 0) {
				if (state == 1) {
					this.params.match(param);
				} else if (state == 2) {
					this.params.count(Integer.parseInt(paramString));
				}
				state = 0;
				continue;
			}
			
			if (paramString.equals(Keyword.MATCH.name())) {
				state = 1;
			} else if (paramString.equals(Keyword.COUNT.name())) {
				state = 2;
			}
		}
	}
	
	public byte[] getCursor() {
		return cursor;
	}

	public void setCursor(byte[] cursor) {
		this.cursor = cursor;
	}

	@Override
	public byte[][] getParam() {
		return new byte[][]{SafeEncoder.encode(getKey())};
	}
	
	@Override
	public ibsp.cache.client.protocol.ScanResult<Map<byte[], byte[]>> doExecute(NJedis jedis) throws Exception {
		
		redis.clients.jedis.ScanResult<Map.Entry<byte[], byte[]>> result =
				jedis.hscan(SafeEncoder.encode(getKey()), getCursor(), getParams());
		ibsp.cache.client.protocol.ScanResult<Map<byte[], byte[]>> result2 = 
				new ibsp.cache.client.protocol.ScanResult<Map<byte[], byte[]>>();
		Map<byte[], byte[]> result3 = new HashMap<byte[], byte[]>();
		
		long len = 0;
	    List<Map.Entry<byte[], byte[]>> results = result.getResult();
        for(Map.Entry<byte[], byte[]> entry : results) {
			len += (entry.getKey().length + entry.getValue().length);
			result3.put(entry.getKey(), entry.getValue());
			result2.setResults(result3);
        }
        result2.setCursor(result.getCursorAsBytes());
        
		setRespLength(len);
		return result2;
	}
	
	@Override
	public String toString() {
		return "[key="+getKey()+"]";
	}
}
