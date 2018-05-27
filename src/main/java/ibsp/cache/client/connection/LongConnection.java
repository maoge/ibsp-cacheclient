package ibsp.cache.client.connection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.connection.ParaConversion;
import ibsp.cache.client.core.Nheader;
import ibsp.cache.client.core.ParaEntity;
import ibsp.cache.client.protocol.Protocol.Command;

public abstract class LongConnection {

	protected static Logger logger = LoggerFactory.getLogger(LongConnection.class);
	protected String connname;
	protected ConnectionStatus connectionStatus;
	protected String host;
	protected int port;
	protected Socket socket;
	protected int timeout;
	protected int checkerTimeOut;
	protected Map<Exception, String> excepts;
	
	protected enum ConnectionStatus {
		OK, CLOSED, STALE, RETRY, DOWN
	}

	public LongConnection(String host, int port, ParaConversion paraconversion ) {
		this(host, port, paraconversion, 30);
	}

	public LongConnection(String host, int port, ParaConversion paraconversion, int timeOut ) {
		this(host, port, paraconversion, timeOut, null);
	}

	/**
	 * @param host
	 * @param port
	 * @param paraconversion
	 * @param timeOut  秒
	 * @param connectionName
	 */
	public LongConnection(String host, int port, ParaConversion paraconversion, int timeOut, String connectionName ) {
		
		this.host = host;
		this.port = port ;
		this.connname = connectionName;
		this.connectionStatus = ConnectionStatus.STALE;
		this.timeout = timeOut * 1000;
        this.checkerTimeOut = this.timeout/3>1000 ? 1000 : this.timeout/3;  
	}

	public byte[] call(ParaEntity request) throws InterruptedException, TimeoutException {
		return call(request, timeout);
	}

	public abstract byte[] call(ParaEntity request, int timeout) throws InterruptedException, TimeoutException;

	public abstract ParaConversion getParaconversion();

	public int getTimeout() {
		return timeout;
	}

	public String getConnname() {
		return connname;
	}

	public void setConnname(String connname) {
		this.connname = connname;
	}

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}
	
	public void setExceptionList(Map<Exception, String> excepts) {
		this.excepts = excepts;
	}

	/**
	 * 重连连接
	 * @return 重连是否成功
	 */
	public abstract boolean reconnection();
	
	protected void makeSocket() throws Exception {
		connectionStatus = ConnectionStatus.RETRY;
		socket = new Socket();
		socket.setReuseAddress(true);
		socket.setKeepAlive(true); 
		socket.setTcpNoDelay(true); 
		socket.setSoLinger(true, 0);

		socket.connect(new InetSocketAddress(host, port), timeout);
		logger.warn("重连"+host+",port="+port+"成功!" );
	}

	/**
	 * 关闭连接
	 */
	public abstract void close();
	
	/**
	 * 释放连接
	 */
	public abstract void release();

	
	public void check() throws Exception {
		if(isConnected()) {
			try {
				call(new ParaEntity(Command.PING, new Nheader("test"), new byte[][] {"0".getBytes()}), checkerTimeOut);
			} catch (Exception e) {
				logger.error(getConnname() + "发送ping包失败! Cause:" + e.toString());
				throw e;
			}
		}
	}
	
	public boolean isConnected() {
		return (this.socket != null) && (this.socket.isBound()) && (!this.socket.isClosed())&& (this.socket.isConnected()) 
				&& (!this.socket.isInputShutdown()) && (!this.socket.isOutputShutdown()) 
				&& (this.connectionStatus == ConnectionStatus.OK);
	}
	public void addException(Exception e, Socket old) {	
		if (old == this.socket || this.socket == null) {
			synchronized (this) {
				if (old == this.socket || this.socket == null) {
					synchronized(this.excepts) {
						this.excepts.put(e, connname);
						this.excepts.notify();
					}
				}
			}
		}
	}

}
