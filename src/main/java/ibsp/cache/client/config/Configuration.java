package ibsp.cache.client.config;

import java.util.concurrent.locks.ReentrantLock;

import ibsp.common.utils.CONSTS;
import ibsp.common.utils.PropertiesUtils;

/***
 * 配置信息类
 */
public class Configuration {
	private static final ReentrantLock monitor = new ReentrantLock();
	private static Configuration instance = null;
	
	private int poolSize          = 1;
	private String connectionMode = "sync";
	private int redisProxyTimeout = 30;
	private String metasvrUrl     = "";
	private String serviceID      = "";
	
    public static Configuration getInstance() {
    	monitor.lock();
    	try {
            if(instance==null) instance = new Configuration();;
    	} finally {
    		monitor.unlock();
    	}
    	return instance;
    }
    
	private Configuration() {
		PropertiesUtils pUtils = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE);
		this.poolSize          = pUtils.getInt(CONSTS.POOL_SIZE, 1);
		this.connectionMode    = pUtils.get(CONSTS.CONNECTION_MODE, "sync");
        this.redisProxyTimeout = pUtils.getInt(CONSTS.REDIS_PROXY_TIMEOUT, 30);
        this.metasvrUrl        = pUtils.get(CONSTS.METASVR_ROOTURL, "");
        this.serviceID         = pUtils.get(CONSTS.CONS_SERVICE_ID, "");
	}

	public int getPoolSize() {
		return poolSize;
	}

	public String getConnectionMode() {
		return connectionMode;
	}

	public int getRedisProxyTimeout() {
		return redisProxyTimeout;
	}

	public String getMetasvrUrl() {
		return metasvrUrl;
	}
	
	public String getServiceID() {
		return serviceID;
	}
	
}
