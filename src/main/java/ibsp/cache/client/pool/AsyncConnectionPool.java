package ibsp.cache.client.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.exception.RedisException;
import ibsp.common.utils.IBSPConfig;

public class AsyncConnectionPool extends ConnectionPool {
	
	private long checkerSleepTime = 2000;
	private AtomicInteger maxUseCount = new AtomicInteger(1);
	
	private volatile boolean check = false;
	private Checker checker;
	private Reconnector reconnector;
	private ExecutorService executor;
	
	public AsyncConnectionPool(String groupID) {
		super(groupID);
		int timeout = IBSPConfig.getInstance().getCacheRedisProxyTimeout();
        this.checkerSleepTime = timeout*1000/3>1000 ? 1000 : timeout*1000/3;
        
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
        int takeCount = 0, maxUse = this.maxUseCount.get();
        Poolable<NJedis> freeObject = null;
        while (!available) {
        	
        	if (takeCount>=poolList.size()) {
        		takeCount = 0;
        		if (this.maxUseCount.get()==maxUse) 
        			this.maxUseCount.set(maxUse+1);
        		maxUse = this.maxUseCount.get();
        		logger.warn("增加连接最大等待请求数至："+this.maxUseCount);
        	}
        	
        	AtomicReference<Poolable<NJedis>> poolable = this.poolList.get((int)(count.incrementAndGet() % poolList.size()));
        	if (poolable==null) continue;
        	if (poolable.get().getObject()==null || !poolable.get().getObject().isConnected()) continue;
        	freeObject = poolable.get();
        	if (freeObject.getUseCount().get()>=maxUse) {
        		takeCount++;
        		continue;
        	} else {
        		freeObject.getUseCount().incrementAndGet();
        		available = true;
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
    	freeObject.getUseCount().decrementAndGet();
    }
	
	@Override
	public void addProxy(String proxyName) {
		super.addProxy(proxyName);
		maxUseCount.set(1);
		logger.warn("重连成功，连接最大等待请求数重新计算！");
	}
	
	@Override
    public void decreaseProxy(String proxyName) {
		super.decreaseProxy(proxyName);
		maxUseCount.set(1);
		logger.warn("重连成功，连接最大等待请求数重新计算！");
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
						if (checkException()) {
							maxUseCount.set(1);
							logger.warn("重连成功，连接最大等待请求数重新计算！");
						}
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
     * Checker定期向接入机发ping包，防止接入机断网或宕机
     */
	private class Checker implements Runnable{
		
		private int exception_count = 0;
		private final int TIMEOUT_MAX_COUNT = 3;
		private final long TOP = Long.MAX_VALUE-10000000;
		
		@Override
		public void run() {
			
			while (check) {
				//防止count越界
				if (count.get()>TOP) {
					count.set(0);
				}
				try {
					for (int i=0; i<poolList.size(); i++) {
						
						AtomicReference<Poolable<NJedis>> reference = poolList.get(i);
						exception_count=0;
						while (exception_count < TIMEOUT_MAX_COUNT && poolList.contains(reference)) {
							try {
								reference.get().getObject().check();
								break;
							} catch (Exception e) {
								logger.warn("连接"+reference.get().getObject().getName()+"发送ping包失败: "+e.getMessage());
								if(e instanceof RedisException) {
				    				String errorMsg = e.getMessage();
				    				if(errorMsg.indexOf("timeout")!=-1) {
				    					continue;
				    				}
								}
								exception_count++;
								if (exception_count < TIMEOUT_MAX_COUNT) {
									Thread.sleep(checkerSleepTime);
								}
							}
						}
						
						if (exception_count == TIMEOUT_MAX_COUNT) {
							List<String> conns = getAllConnByName(reference.get().getObject().getName());
							synchronized (excepts) {
								for (String conn : conns) {
									logger.info(conn+" add exception.");
									excepts.put(new RuntimeException("reconnect"), conn);
								}
								excepts.notify();
							}
						}
					}

	    		} catch (Exception e) {
	    			logger.error("Checker线程异常", e);
	    		}
				
				try {
					Thread.sleep(checkerSleepTime);
				} catch (InterruptedException e) {}
			}
		}
		
	    private List<String> getAllConnByName(String connName) {
	    	List<String> result = new ArrayList<String> ();
	    	String proxyName = connName.split("_")[1];
	    	
	    	for (int i=0; i<poolList.size(); i++) {
	    		String name = poolList.get(i).get().getObject().getName();
	    		if (name.contains(proxyName)) {
	    			result.add(name);
	    		}
	    	}
	    	
	    	return result;
	    }
	}
}
