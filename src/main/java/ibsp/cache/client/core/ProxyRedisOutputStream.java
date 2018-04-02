package ibsp.cache.client.core;

import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import redis.clients.util.RedisOutputStream;

public class ProxyRedisOutputStream {
	
	private RedisOutputStream resisOut;
	private OutputStream out;
	private Lock modifyLock = new java.util.concurrent.locks.ReentrantLock();
	
	public ProxyRedisOutputStream() {
	}
	
	public RedisOutputStream getSameRedisOutputStream(OutputStream out) {
		if (out != this.out) {
			if (this.out !=null) {
				try {
					this.out.close();
				} catch (Exception e) {
				}
			}
			if (this.resisOut !=null) {
				try {
					resisOut.close();
				} catch (Exception e) {
				}
			}
			try {
				modifyLock.lock();
				if (out != this.out) {
					this.out = out;
					this.resisOut =  new RedisOutputStream(out);
				}
			} finally {
				modifyLock.unlock();
			}
		}
		return this.resisOut;
	}
}
