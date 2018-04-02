package ibsp.cache.client.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import ibsp.cache.client.config.Configuration;
import ibsp.cache.client.core.NJedis;

/**
 * 同步连接池
 * @author xiaoyn
 * @since v1.0.17
 */
public class SyncConnectionPool extends ConnectionPool {

	private final long CHECKER_SLEEP_TIME = 1000;
	
	private volatile boolean check = false;
	private Checker checker;
	private Reconnector reconnector;
	private ExecutorService executor;
	
	public SyncConnectionPool(String groupID) {
		super(groupID);
		
		check = true;
		this.executor = Executors.newFixedThreadPool(2);
		this.checker = new Checker();
		this.reconnector = new Reconnector();
		executor.execute(this.checker);
		executor.execute(this.reconnector);
	}
	
	@Override
    public Poolable<NJedis> borrowObject() {
        if (shuttingDown) {
            throw new IllegalStateException("Cannot borrow object! Connection pool is shutting down!");
        }
        if (poolList.size() == 0) {
        	return null;
        }
        
        boolean available = false;
        Poolable<NJedis> freeObject = null;
        
        while (!available) {
        	AtomicReference<Poolable<NJedis>> poolable = null;
        	try {
        		poolable = this.poolList.get((int)(count.incrementAndGet() % poolList.size()));
        	} catch (IndexOutOfBoundsException e) {
        		continue;
        	}
        	if (poolable==null) continue;
        	if (poolable.get().getObject()==null || !poolable.get().getObject().isConnected()) continue;
        	freeObject = poolable.get();
        	
        	synchronized (freeObject) {
        		if (freeObject.isUsing()) {
        			continue;
        		} else {
        			freeObject.setUsing(true);
        			freeObject.setLastTime(System.currentTimeMillis());
        			available = true;
        		}
        	}
        }

        return freeObject;
    }
	
	@Override
	public synchronized void shutdown() {
        check = false;
        this.checker = null;
        this.reconnector = null;
        this.executor.shutdown();
        super.shutdown();
	}
	
	@Override
    public void returnObject(Poolable<NJedis> freeObject) {	
    	synchronized (freeObject) {
    		freeObject.setUsing(false);
    	}
    }
	
	//内部类
    /**
     * Reconnetor反复检查异常列表，并发起重连
     */
	private class Reconnector implements Runnable {

		@Override
		public void run() {
			while (check) {
				try {
					if (excepts.size()==0) {
						synchronized (excepts) {
							try {
								excepts.wait(60*1000);
							} catch (InterruptedException e) {
								logger.error("", e);
							}
						}
					} else {
						checkException();
						Thread.sleep(3000);
					}
				} catch (Exception e) {
					logger.error("Reconnector线程异常", e);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						logger.error("Reconnector线程异常", e1);
					}
				}
			}
		}
	}
	
    /**
     * Checker检查是否有超时未释放的连接，以及向长久未使用的连接发送ping包
    */
	private class Checker implements Runnable{
		
		private final long IDLE_TIME = Configuration.getInstance().getRedisProxyTimeout()*1000*2;
		private final long TOP = Long.MAX_VALUE-10000000;
		
		@Override
		public void run() {
			
			while (check) {
				
				try {
					//防止count越界
					if (count.get()>TOP) {
						count.set(0);
					}
				
					for (AtomicReference<Poolable<NJedis>> reference : poolList) {
						Poolable<NJedis> object = reference.get();
						long now = System.currentTimeMillis();
						boolean ping = false;
					
						synchronized (object) {
							if (now-object.getLastTime()>IDLE_TIME) {
								if (object.isUsing()) {
									//如果有异常导致超时连接没有回收，在这里进行回收
									object.setUsing(false);
								} else {
									//长时间没有使用的连接，发送ping包
									ping = true;
									object.setUsing(true);
									object.setLastTime(System.currentTimeMillis());
								}
							}
						}
					
						if (ping) {
							try {
								object.getObject().check();
							} catch (Exception e) {
								logger.error("连接"+object.getObject().getName()+"发送ping包失败！"+e.getMessage());
							} finally {
								returnObject(object);
							}
						}
					}
				} catch (Exception e) {
					logger.error("Checker线程异常！", e);
				}
				
				try {
					Thread.sleep(CHECKER_SLEEP_TIME);
				} catch (InterruptedException e) {}
			}
		}
	}
}
