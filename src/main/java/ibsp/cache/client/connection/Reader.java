package ibsp.cache.client.connection;

import java.io.InputStream;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;import org.slf4j.LoggerFactory;

public class Reader implements Runnable {
	public static final Logger logger = LoggerFactory.getLogger(Reader.class);
	private AsyncConnection connection;
	private InputStream in;
	
	public Reader(final AsyncConnection connection) {
		this.connection = connection;
	}
	
	public void setIn(InputStream in) {
		this.in = in;
	}

	@Override
	public void run() {
		
		while (connection.isRun()) {
			if (in == null) {
				continue;
			}
			
			try {
				this.connection.getParaconversion().read(in);
			} catch (SocketTimeoutException e) {
				logger.debug(connection.getConnname() + "读线程超时! Timeout:"+ connection.getTimeout(),e);
			} catch (Exception e) {
				logger.error(connection.getConnname() + " 读取报文异常! Cause:" + e.toString());
				this.connection.addException(e,in);
				//避免进入读线程异常死循环
                synchronized(this.connection.getReadLock()) {
                    try {
                        this.connection.getReadLock().wait();
                    } catch (InterruptedException e1) {}
                }
			} finally {
				if(!connection.isRun()) {
					break;
				}
			}
		}
	}
}
