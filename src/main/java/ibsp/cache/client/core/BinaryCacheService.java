package ibsp.cache.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.config.MetasvrConfigFactory;
import ibsp.cache.client.exception.CacheServiceException;
import ibsp.cache.client.exception.CacheServiceException.CacheServiceErrorInfo;
import ibsp.cache.client.pool.ConnectionPool;
import ibsp.cache.client.structure.CacheRequest;
import ibsp.cache.client.structure.CacheResponse;
import ibsp.cache.client.structure.Get;
import ibsp.cache.client.structure.Getset;
import ibsp.cache.client.structure.Set;
import ibsp.cache.client.structure.SetEx;
import ibsp.cache.client.structure.SetNx;
import ibsp.cache.client.utils.Global;

public class BinaryCacheService implements IBinaryCacheService {
	protected static final Logger log = LoggerFactory.getLogger(BinaryCacheService.class);
	protected static final Long   FAILED_RESULT = 0L;
	protected static final Long   SUCCESS_RESULT = 1L;
	private String innerGroupId;
	private static MetasvrConfigFactory metasvrConfigFactory;
	
	public BinaryCacheService(String serviceID, String metasvrUrl) {
		this.innerGroupId = serviceID;
		metasvrConfigFactory = MetasvrConfigFactory.getInstance(metasvrUrl);		
		metasvrConfigFactory.addGroup(serviceID);
	}
		
	@Override
	public String set(String key, byte[] value) {
		CacheRequest<Set> request = new CacheRequest<Set>();
		Set param = new Set();
		param.setKey(key);
		param.setByteValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);
		return resp.getCode();
	}
	
	@Override
	public String set(String key, byte[] value, int expireSeconds) {
		CacheRequest<Set> request = new CacheRequest<Set>();
		Set param = new Set();
		param.setKey(key);
		param.setByteValue(value);
		param.setSecond(expireSeconds);
		request.setParam(param);
		CacheResponse resp = execute(request);
		return resp.getCode();
	}

	@Override
	public byte[] getBytes(String key) {
		CacheRequest<Get> request = new CacheRequest<Get>();
		Get param = new Get();
		param.setKey(key);
		param.setByteValue(true);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if (resp.getCode().equals(CacheResponse.OK_CODE) && resp.getResult() != null) {
			return (byte[])resp.getResult();
		}
		return null;
	}

	@Override
	public byte[] getSet(String key, byte[] value) {
		CacheRequest<Getset> request = new CacheRequest<Getset>();
		Getset param = new Getset();
		param.setKey(key);
		param.setByteValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if (resp.getCode().equals(CacheResponse.OK_CODE) && resp.getResult() != null) {
			return (byte[])resp.getResult();
		}
		return null;
	}
	
	@Override
	public String setex(String key, int seconds, byte[] value) {
		CacheRequest<SetEx> request = new CacheRequest<SetEx>();
		SetEx param = new SetEx();
		param.setCommand("SETEX");
		param.setKey(key);
		param.setByteValue(value);
		param.setSeconds(seconds);
		request.setParam(param);
		CacheResponse resp = execute(request);
		return resp.getCode();
	}

	@Override
	public Long setnx(String key, byte[] value) {
		CacheRequest<SetNx> request = new CacheRequest<SetNx>();
		SetNx param = new SetNx();
		param.setCommand("SETNX");
		param.setKey(key);
		param.setByteValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);
        if(CacheResponse.OK_CODE.equals(resp.getCode())) {
        	return (Long)resp.getResult();
        }
        return FAILED_RESULT;
	}

	protected CacheResponse execute(final CacheRequest<?> request) {
		
		CacheResponse response = null;
		try {
			if (request.getGroupId() == null) {
				if (innerGroupId == null)
					return CacheResponse.errorOf(request.getRequestId(), "缓存分组名称参数为空!");
				
				request.setGroupId(innerGroupId);
			}
		    
	        if(!metasvrConfigFactory.hasGroupId( request.getGroupId() )) {
	        	return CacheResponse.errorOf(request.getRequestId(), "缓存分组名称参数与默认分组名称不符!");
	        }
		    		    	        
	        request.setRemoteIP("0.0.0.0");
	        request.setRequestTime(System.currentTimeMillis());
	        request.complieRequestLength();
	        
	        try {
	        	ConnectionPool pool = Global.poolList.get(request.getGroupId());
	        	response = pool.call(request);
	        } catch(CacheServiceException e) {
	        	if(e.getErrorCode() == CacheServiceErrorInfo.e4.getErrorCode()) {
	        		log.error("连接[" + e.getConnName() + "]执行redis请求超时!, 错误码:" + e.getErrorCode());
	        		response = CacheResponse.errorOf(request.getRequestId(), "执行redis请求超时!");
	        	} else if (e.getErrorCode() == CacheServiceErrorInfo.e5.getErrorCode()) {
	        		log.error("连接[" + e.getConnName() + "]执行redis请求返回数据错误!, 错误码:" + e.getErrorCode());
	        		response = CacheResponse.errorOf(request.getRequestId(), "执行redis请求返回数据错误!");
	        	} else if(e.getErrorCode() == CacheServiceErrorInfo.e1.getErrorCode()) {
	        		log.error("连接[" + e.getConnName() + "]连接失败, 错误码:" + e.getErrorCode());
	        	}
	        	throw e;
	        }
	            
	        if(response == null) {
	        	log.error("缓存分组[" + request.getGroupId() + "]不存在可用接入机!");   
	        	response = CacheResponse.errorOf(request.getRequestId(), "缓存分组[" + request.getGroupId() + "]不存在可用接入机!");	                
	        }
		} finally {}
		
        return response;
	}
			
	public void close() {
		try {
			metasvrConfigFactory.close();
		} catch(Exception e) {
			log.error("关闭CacheService异常！", e);
		}
	}

}
