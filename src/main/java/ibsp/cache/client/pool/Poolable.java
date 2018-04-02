package ibsp.cache.client.pool;

import java.io.Closeable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version v1.0.16
 */
public class Poolable<T> implements Closeable {
    private final T object;
    private boolean using; //同步连接：是否正在使用
    private long lastTime; //同步连接：上次使用的时间
    private final AtomicInteger useCount; //异步连接：正在使用该连接的线程数
    private final String name;


    public Poolable(T t) {
    	this.name = UUID.randomUUID().toString();
        this.object = t;
        this.using = false;
        this.lastTime = System.currentTimeMillis();
        this.useCount = new AtomicInteger(0);
    }

    public String getName() {
        return this.name;	
    }
    
    public T getObject() {
        return object;
    }
	
	public boolean isUsing() {
		return using;
	}
	
	public void setUsing(boolean using) {
		this.using = using;
	}
	
	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public AtomicInteger getUseCount() {
		return useCount;
	}
	
	@Override
    public void close() {
        
    }
}
