package ibsp.cache.client.structure;

import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("rawtypes")
public class CacheRequest<T extends Operate> {
	private static AtomicLong ids = new AtomicLong();
	private static final long MAX_IDS = 1000000000000000000l;
	private String requestId;
	private String groupId;
	private String remoteIP;
	private long   requestTime;
	private T param;
	
	public enum LengthType {
		SMALL(1024, "<=1K"), MIDDLE(32*1024, ">1K"), LARGE(Integer.MAX_VALUE, ">32K");
		private final int value;
		private final String name;
		
		LengthType(int length, String name) {
           this.value = length;
           this.name = name;
		}
		
		public int getValue() {
			return this.value;
		}
		
		public String getName() {
			return this.name;
		}
	}
	
	public CacheRequest() {
	    long id = ids.incrementAndGet();
	    if (id > MAX_IDS) {
	    	ids.set(0);
	    }
	    requestId = String.valueOf(id);
	}

	public T getParam() {
		return param;
	}

	public void setParam(T param) {
		this.param = param;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
		if(getParam()!=null) {
			getParam().setGroupId(groupId);
		}
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}
	
	public void complieRequestLength() {
	    if(getParam()!=null) {
	        long resqLength = 0L;
	        for(byte[] param : getParam().getParam()) {
	            resqLength += param.length;
	        }
	        getParam().setResqlength( resqLength );
	    }
	}
	
	public LengthType getRequestLengthType() {
	    long resqLength = getParam()!=null ? getParam().getResqlength() : 0L;
		if(resqLength > 0 && resqLength <= LengthType.SMALL.value) {
			return LengthType.SMALL;
		} else if(resqLength > LengthType.SMALL.value && resqLength <= LengthType.MIDDLE.value) {
		    return LengthType.MIDDLE;
		}
        return LengthType.LARGE;
	}
}
