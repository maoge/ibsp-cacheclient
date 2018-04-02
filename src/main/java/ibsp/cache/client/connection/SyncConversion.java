package ibsp.cache.client.connection;

import java.io.IOException;
import java.io.InputStream;

import ibsp.cache.client.core.Respond;
import ibsp.cache.client.exception.ProxyRedisException;

public class SyncConversion extends ParaConversion {
	
	public byte[] read(final InputStream is, final long id) throws ProxyRedisException, IOException {
	
		byte[] resp = null;
		boolean ok = false;
		
		while (!ok) {
			Respond respond = readRespond(is);
			if (respond!=null && respond.getReqId() == id) {
				ok = true;
				resp = respond.getResp();
			}
		}
	
		return resp;
	}
}
