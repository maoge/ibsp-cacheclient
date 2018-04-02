package ibsp.cache.client.connection;

import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.core.ParaEntity;

public class Writer implements Runnable {
	public static final Logger logger = LoggerFactory.getLogger(Writer.class);
	private AsyncConnection connection;
	private java.util.concurrent.ArrayBlockingQueue<ParaEntity> sends = new ArrayBlockingQueue<ParaEntity>(10000);
	private OutputStream out = null;

	public Writer(AsyncConnection longConnection) {
		this.connection = longConnection;
	}
	
	public void clear() {
		sends.clear();
	}
	
	public boolean offer(ReqSession session, long timeout, TimeUnit unit) throws InterruptedException {
		connection.getParaconversion().addRequestSession(session);
		return sends.offer(session.getRequest(), timeout, unit);
	}
	
	public void setOut(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public void run() {
		
		while(connection.isRun()) {
			try {
				if(out == null){
					continue;
				} else {
					ParaEntity request = sends.take();
					
					if (request != null) {
						connection.getParaconversion().write(request, out);
					} else {
						continue;
					}
				}
			} catch (Exception e) {
				
			    if (!(e instanceof InterruptedException)) {
	                logger.error(connection.getConnname() + " 发送报文异常! Cause:" + e.toString());
	                this.connection.addException(e, out);
	                //避免在产生网络异常时继续作发送操作
	                synchronized (this.connection.getWriteLock()) {
	                    try {
	                    	this.connection.getWriteLock().wait();
	                    } catch (InterruptedException e1) {}
	                }
			    }
			} finally {
				if(!connection.isRun()) {
					break;
				}				
			}
		}
	}
}
