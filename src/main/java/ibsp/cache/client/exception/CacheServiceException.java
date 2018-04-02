package ibsp.cache.client.exception;

public class CacheServiceException extends RuntimeException {
    private static final long serialVersionUID = 3912429853904701250L;
    private CacheServiceErrorInfo errorInfo;
	private int errorCode = -1;
	private String errorMsg = "";
	private String connName;
    
	public static enum CacheServiceErrorInfo {
		DEFAULT(20010000, "~返回结果为空!"),
		e1(20010001, "~redis代理主机连接丢失!"),
		e2(20010002, "~无可用的redis代理主机!"),
		e3(20010003, "~执行redis请求时出错!"),
		e4(20010004, "~执行redis请求超时!"),
		e5(20010005, "~redis返回数据错误!"),
		e6(20010006, "~redis返回数据为null!"),
		e7(20010007, "~接入机初始化未完成!"),
		e8(20010008, "程序类bug!"),
		e9(20010009, "参数为空"),
		e10(20010010, "请求key为空"),
		e11(20010011, "没有该分组权限"),
		e12(20010012, "缓存客户端初始化失败")
        ;
		
		private int errorCode;
		private String errorMsg;

		private CacheServiceErrorInfo(int errorCode, String errorMsg) {
			this.errorCode = errorCode;
			this.errorMsg = errorMsg;
		}

		public int getErrorCode() {
			return this.errorCode;
		}

		public String getErrorMsg() {
			return errorMsg;
		}
	}

	
    public CacheServiceException(CacheServiceErrorInfo errorInfo) {
    	super(errorInfo.getErrorMsg());
    	this.errorInfo = errorInfo;
    }
    
    public CacheServiceException(String errorCode, String message) {
    	super(message);
    	this.errorCode = Integer.parseInt(errorCode);
    	this.errorMsg  = message;
    }
    
    public CacheServiceException(String message) {
        super(message);
        this.errorMsg  = message;        
    }
    
    public CacheServiceException(Throwable cause) {
        super(cause);
    }
    
    public CacheServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheServiceException(String message, CacheServiceErrorInfo errorInfo, Throwable cause) {
        super(message, cause);
        this.errorInfo = errorInfo;
    }
        
    public CacheServiceException(String message, Throwable cause, boolean enableSuppression,  boolean writableStackTrace) {
        super(message, cause);
    }
    
    public int getErrorCode() {
    	return this.errorInfo==null ? this.errorCode : this.errorInfo.getErrorCode();
    }
    
    public String getErrorMsg() {
    	return this.errorInfo==null ? this.errorMsg : this.errorInfo.getErrorMsg();
    }
    
	public String getConnName() {
		return connName;
	}

	public void setConnName(String connName) {
		this.connName = connName;
	}
}
