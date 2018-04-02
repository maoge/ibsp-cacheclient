package ibsp.cache.client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.core.Respond;
import ibsp.cache.client.exception.ProxyRedisException;

public class AsyncConversion extends ParaConversion{
	public static final Logger logger = LoggerFactory.getLogger(AsyncConversion.class);
	private static final ConcurrentSkipListMap<Object, ReqSession> allrequest = new ConcurrentSkipListMap<Object, ReqSession>();
	
	public void addRequestSession(ReqSession session) {
		allrequest.put(session.request.getReqId(), session);		
	}
	
	public void removeRequestSession(ReqSession session) {
		allrequest.remove(session.request.getReqId());
	}
	
	
	public byte[] waitResult(long timeout, ReqSession session) throws InterruptedException, TimeoutException {
		
		try {
			synchronized (session) {
				session.wait(timeout);
			}
			
			if (session.respond == null) {
				session.setEndResptime(System.currentTimeMillis());
				session.setProcessTimes(Math.round((session.getEndResptime() - session.getStartReqTime()) / 1000));
				logger.warn("CacheClient Request Timeout, ReqId:" + session.request.getReqId());
				
				throw new TimeoutException("CacheClient Request Timeout, ReqId:" + session.request.getReqId() + ", processTimes:" + session.processTimes + ", timeout:" + timeout);
			}
		} finally {
			allrequest.remove(session.request.getReqId());
		}
		return session.respond;
	}
	
	public void notifClient(Object reqId, byte[] resp) {
		ReqSession session = null;
		try {
			session = allrequest.get(reqId);
			if(session!=null) {
				session.setEndResptime(System.currentTimeMillis());
				session.setProcessTimes(Math.round((session.getEndResptime() - session.getStartReqTime()) / 1000));
				session.setRespond(resp) ;
				synchronized (session) {
					session.notifyAll();
				}
			} else {
				logger.warn("CacheClient Request Data Missing." + reqId);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void read(final InputStream in) throws ProxyRedisException, IOException {
		Respond respond = readRespond(in);
		if (respond != null) {
			notifClient(respond.getReqId(), respond.getResp());
		}
	}
}
