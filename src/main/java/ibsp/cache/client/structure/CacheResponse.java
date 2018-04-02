package ibsp.cache.client.structure;

import ibsp.cache.client.exception.CacheServiceException;
import ibsp.cache.client.structure.CacheRequest.LengthType;

public class CacheResponse {
    public static final String ERROR_CODE = "-1";
    public static final String OK_CODE = "0";
	private String requestId;
	private String code;
	private Object result;
	private long   responseTime;
	private long   responseLength;
	private static final String SET_OK = "OK";

    public static CacheResponse codeOf(String id, String code, Object result, long respLength)
    {
    	CacheResponse response;
        response = new CacheResponse();
        response.setId(id);
        response.setResult(result);
        response.setResponseLength(respLength);
        response.setResponseTime(System.currentTimeMillis());
        response.setCode(code);
        return response;
    }
    
    public String takeSetResult() {
    	if (result.equals(SET_OK)) {
    		return OK_CODE;
    	} else {
    		return ERROR_CODE;
    	}
    }
    
    public static CacheResponse errorOf(String id, String result)
    {
        return codeOf(id, ERROR_CODE, result, result.getBytes().length);
    }
    
    public static CacheResponse errorOf(String id, String errorCode, String result)
    {
        return codeOf(id, errorCode, result, result.getBytes().length);
    }
    
    public static CacheResponse errorOf(String id, CacheServiceException cacheServiceException)
    {
        return codeOf(id, String.valueOf(cacheServiceException.getErrorCode()), cacheServiceException.getErrorMsg(), cacheServiceException.getErrorMsg().getBytes().length);
    }
    
    public static CacheResponse okOf(String id, Object result, long respLength)
    {
        return codeOf(id, OK_CODE, result, respLength);
    }

	public String getId() {
		return requestId;
	}

	public void setId(String id) {
		this.requestId = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public long getResponseLength() {
		return responseLength;
	}

	public void setResponseLength(long responseLength) {
		this.responseLength = responseLength;
	}

	public LengthType getResponseLengthType() {
		if(getResponseLength() > 0 && getResponseLength() <= LengthType.SMALL.getValue()) {
			return LengthType.SMALL;
		} else if(getResponseLength() > LengthType.SMALL.getValue() && getResponseLength() <= LengthType.MIDDLE.getValue()) {
		    return LengthType.MIDDLE;
		}
        return LengthType.LARGE;
	}
	
}
