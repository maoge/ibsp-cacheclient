package ibsp.cache.client.exception;

@SuppressWarnings("serial")
public class ProxyRedisException extends Exception {
	
	public static enum PROXYERROR {
		DEFAULT(50012000, false, false),//默认错误
		e1(50012001, true, false),// 路由队列堵塞
		e2(50012002, true, false),// 添加路由队列异常
		e3(50012003, true, false),// 路由处理异常
		e4(50012004, true, true),// 注册客户端读失败
		e5(50012005, true, true),// 超过�?大允许的客户端连接数
		e6(50012006, true, false),// 注册目标路由读事件异�?
		e7(50012007, true, false),// 单个客户端连接请求数大于规定�?
		e8(50012008, true, false),// 协议异常
		e9(50012009, true, false),// 不完整的报文
		e10(50012010, true, false),// 报文超长
		e11(50012011, false, true),// 网络io异常
		e12(50012012, true, true),// 目标服务器繁�?,稍后再试
		e13(50012013, true, false),// 获取目标路由主机失败
		e14(50012014, true, false),// 路由主机配置端口不合�?
		e15(50012015, true, false),// 连接目标主机失败
		e16(50012016, true, false),// 目标主机网络繁忙
		e17(50012017, true, false),// 路由发�?�给目标主机时网络错�?
		e18(50012018, true, false),// 读取解析客户端请求失�?
		e19(50012019, true, false),// 注册redis服务端返回失�?
		e20(50012020, true, false),// redis read io exception
;
		private int value;     //
		private boolean bSendBack;  // 是否�?客户端回写错误信�?
		private boolean bCloseClient;  // 是否关闭对端连接


		private PROXYERROR(int s, boolean bBack, boolean bClose) {
			// 定义枚举的构造函�?
			value = s;
			bSendBack = bBack;
			bCloseClient = bClose;
		}
		
		public int getValue() {
			// 得到枚举值代表的字符串�??
			return value;
		}
		
		public boolean getSendBack() {
			return bSendBack;
		}
		
		public boolean getCloseClient() {
			return bCloseClient;
		}
	}
	
	private int errorCode;
	private boolean bSendBack;  // 是否�?客户端回写错误信�?
	private boolean bCloseClient;  // 是否关闭对端连接
	
	public ProxyRedisException() {
		this(50010000, false, false);
	}
	
	public ProxyRedisException(int errorcode, boolean bSendBack, boolean bCloseClient) {
		super();
		this.errorCode = errorcode;
		this.bSendBack = bSendBack;
		this.bCloseClient = bCloseClient;
	}

	public int getErrorCode() {
		return errorCode;
	}
	
	public ProxyRedisException setErrorCode(PROXYERROR errorInfo) {
		this.errorCode = errorInfo.value;
		return this;
	}
	
	public ProxyRedisException setErrorCode(int errorCode) {
		this.errorCode = errorCode;
		return this;
	}
	
	public boolean getSendBack() {
		return bSendBack;
	}
	
	public boolean getCloseClient() {
		return bCloseClient;
	}
	
	public ProxyRedisException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause);
	}

	public ProxyRedisException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ProxyRedisException(String message) {
		super(message);
	}
	public ProxyRedisException(Throwable cause) {
		super(cause);
	}
	
	public ProxyRedisException(String message, int errorcode, boolean bSendBack, boolean bCloseClient, Throwable cause) {
		super(message, cause);
		
		this.errorCode = errorcode;
		this.bSendBack = bSendBack;
		this.bCloseClient = bCloseClient;
	}
	
	public ProxyRedisException(String message, PROXYERROR errorInfo, Throwable cause) {
		super(message, cause);
		
		this.errorCode = errorInfo.value;
		this.bSendBack = errorInfo.bSendBack;
		this.bCloseClient = errorInfo.bCloseClient;
	}

}
