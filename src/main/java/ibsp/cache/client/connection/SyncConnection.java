package ibsp.cache.client.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import ibsp.cache.client.core.ParaEntity;

public class SyncConnection extends LongConnection {

	private int timeoutCount;
	private SyncConversion paraconversion;
	private InputStream in, pin;
	private OutputStream out, pout;
	
	public SyncConnection(String host, int port, SyncConversion paraconversion ) {
		this(host, port, paraconversion, 30);
	}

	public SyncConnection(String host, int port, SyncConversion paraconversion, int timeOut ) {
		this(host, port, paraconversion, timeOut, null);
	}
	
	public SyncConnection(String host, int port, SyncConversion paraconversion, int timeOut, String connectionName) {
		super(host, port, paraconversion, timeOut, connectionName);
		this.paraconversion = paraconversion;
		this.timeoutCount = 0;
	}
	
	@Override
	public byte[] call(ParaEntity request, int timeout) throws InterruptedException, TimeoutException {
		byte[] result = null;
		long startTime = System.currentTimeMillis();
		
		try {
			this.paraconversion.write(request, pout);
		} catch (Exception e) {
			logger.error(this.connname + " 发送报文异常! Cause:" + e.toString());
			this.addException(e);
		}

		try {
			result = this.paraconversion.read(pin, (Long)request.getReqId());
			this.timeoutCount = 0;
		} catch (SocketTimeoutException e) {
			
			//超时的处理
			long endTime = System.currentTimeMillis();
			logger.warn("CacheClient Request Timeout, ReqId:" + request.getReqId());
			this.timeoutCount++;
			if (this.timeoutCount == 3) {
				synchronized (this.excepts) {
					this.excepts.put(e, connname);
					this.excepts.notify();
				}
				this.timeoutCount = 0;
			}
			throw new TimeoutException("CacheClient Request Timeout, ReqId:" + request.getReqId() +
					", processTimes:" + (endTime-startTime) + ", timeout:" + timeout);
			
		} catch (Exception e) {
			logger.error(this.connname + " 读取报文异常! Cause:" + e.toString());
			this.addException(e);
		}
		
		return result;
	}
	
	@Override
	public SyncConversion getParaconversion() {
		return paraconversion;
	}

	@Override
	public boolean reconnection() {
		release();
		synchronized (this) {
			try {
				this.makeSocket();
				socket.setSoTimeout(this.timeout);
				this.in = socket.getInputStream();
				pin = in;
				this.out = socket.getOutputStream();
				pout = out;
				this.notifyAll();
				connectionStatus = ConnectionStatus.OK;
				return true;
			} catch (final Exception e) {
				logger.error("~无法创建redis代理主机连接,节点名:["+getConnname()+"],ip:["+host+"],port:["+port+"]! Cause:" + e.toString());
				this.addException(e, socket);
				socket = null;
				return false;
			}
		}
	}
	
	public void addException(Exception e) {	
		synchronized(this.excepts) {
			this.excepts.put(e, connname);
			this.excepts.notify();
		}
	}
	
	@Override
	public void release() {
		connectionStatus = ConnectionStatus.CLOSED;
		Socket tmp = this.socket;
		java.io.InputStream tin = in;
		java.io.OutputStream tout = out;
		this.socket = null;
		in = null;
		out = null;
		try {
			if (tin!=null){
				tin.close();
			}
		} catch (Exception e) {
			logger.error("close tin", e);
		}
		try {
			if (tout!=null){
				tout.close();
			}
		} catch (Exception e) {
			logger.error("close tout", e);
		}
		try {
			if (tmp!=null){
				tmp.close();
			}
		} catch (Exception e) {
			logger.error("close tmp", e);
		}
	}
	
	@Override
	public void close() {
		this.release();
	}
}
