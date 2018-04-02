package ibsp.cache.client.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ibsp.cache.client.core.ParaEntity;

public class AsyncConnection extends LongConnection {

	private Reader reador;
	private Writer writor;
	private Object readLock = new Object();
	private Object writeLock = new Object();
	private InputStream in;
	private OutputStream out;
	private ExecutorService exector;
	private AsyncConversion paraconversion;
	private volatile boolean run;

	public AsyncConnection(String host, int port, AsyncConversion paraconversion ) {
		this(host, port, paraconversion, 30);
	}

	public AsyncConnection(String host, int port, AsyncConversion paraconversion, int timeOut ) {
		this(host, port, paraconversion, timeOut, null);
	}

	public AsyncConnection(String host, int port, AsyncConversion paraconversion, int timeOut, String connectionName ) {
		super(host, port, paraconversion, timeOut, connectionName);
		this.paraconversion = paraconversion;
		this.run = true;
		this.exector = Executors.newFixedThreadPool(2);
		this.writor = new Writer(this);
		this.reador = new Reader(this);
		this.exector.execute(this.writor);
		this.exector.execute(this.reador);       
	}

	public byte[] call(ParaEntity request, int timeout) throws InterruptedException, TimeoutException {
		ReqSession session = new ReqSession();
		session.setStartReqTime(System.currentTimeMillis());
		session.setProcessTimes(-1);
		session.setRequest(request);

		if (this.writor.offer(session, timeout, TimeUnit.MICROSECONDS)){
			return paraconversion.waitResult(timeout, session);  
		}else {
			throw new TimeoutException("uninit TimeoutException:"+timeout+" ms");
		}   
	}

	@Override
	public AsyncConversion getParaconversion() {
		return paraconversion;
	}
	
	@Override
	public boolean reconnection() {
		release();
		synchronized (this) {
			try {
				this.makeSocket();
				this.writor.clear();
				socket.setSoTimeout(this.timeout);
				this.in = socket.getInputStream();
				this.reador.setIn(in);
				this.out = socket.getOutputStream();
				this.writor.setOut(out);
				
				synchronized(getReadLock()) {
					getReadLock().notify();
				}
				synchronized(getWriteLock()) {
					getWriteLock().notify();
				}
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
		connectionStatus = ConnectionStatus.CLOSED;
		this.run = false;
		this.exector.shutdownNow();
		this.release();
	}

	public boolean isRun() {
		return run;
	}
	
	public Object getReadLock() {
		return readLock;
	}

	public Object getWriteLock() {
		return writeLock;
	}

	
	public void addException(Exception e, java.io.InputStream in) {
		
		if ((in == this.in) || (this.in == null)) {
			synchronized (this) {
				if ((in == this.in) || (this.in == null)) {
					synchronized (this.excepts) {
						this.excepts.put(e, connname);
						this.excepts.notify();
					}
				}
			}
		}
	}

	public void addException(Exception e, java.io.OutputStream out) {
		if ((out == this.out) || (this.out == null)) {
			synchronized (this) {
				if ((out == this.out) || (this.out == null)) {
					synchronized (this.excepts) {
						this.excepts.put(e, connname);
						this.excepts.notify();
					}
				}
			}
		}
	}
}
