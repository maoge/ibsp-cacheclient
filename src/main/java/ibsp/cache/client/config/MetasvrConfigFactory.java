package ibsp.cache.client.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import ibsp.cache.client.exception.CacheServiceException;
import ibsp.cache.client.exception.CacheServiceException.CacheServiceErrorInfo;
import ibsp.cache.client.pool.AsyncConnectionPool;
import ibsp.cache.client.pool.ConnectionPool;
import ibsp.cache.client.pool.SyncConnectionPool;
import ibsp.cache.client.utils.Global;
import ibsp.common.events.EventController;
import ibsp.common.events.EventSubscriber;
import ibsp.common.events.EventType;
import ibsp.common.utils.BasicOperation;
import ibsp.common.utils.CONSTS;
import ibsp.common.utils.HttpUtils;
import ibsp.common.utils.IBSPConfig;
import ibsp.common.utils.MetasvrUrlConfig;
import ibsp.common.utils.SVarObject;
import ibsp.common.utils.StringUtils;

/***
 * 从metaserver获得缓存配置信息, 以单例模式对外提供使用 
 */
public class MetasvrConfigFactory implements EventSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(MetasvrConfigFactory.class);
	private static final ReentrantLock monitor = new ReentrantLock();
	private static MetasvrConfigFactory instance = null;
	
	private Collection<String> globalGroupId;
	private Map<String, Proxy> mapProxy;

	public static MetasvrConfigFactory getInstance() {
		return instance;
	}
	
	public static MetasvrConfigFactory getInstance(String metasvrUrl) {
		monitor.lock();
		try {
			if (instance == null) {
				instance = new MetasvrConfigFactory(metasvrUrl);
			}
		} finally {
			monitor.unlock();
		}
		return instance;
	}
	
	private MetasvrConfigFactory(String metasvrUrl) {
		EventController.getInstance().subscribe(CONSTS.TYPE_CACHE_CLIENT, this);
		
		this.globalGroupId = new HashSet<String>();
		this.mapProxy = new HashMap<String, Proxy>();
	}

	public synchronized void addGroup(String groupId) {
		if (this.hasGroupId(groupId)) 
			return;
		
		this.globalGroupId.add(groupId);
		this.loadConfigInfo(groupId);
		if (IBSPConfig.getInstance().getCacheConnectionMode().equals("sync")) {
			ConnectionPool pool = new SyncConnectionPool(groupId);
			Global.poolList.put(groupId, pool);
		} else if (IBSPConfig.getInstance().getCacheConnectionMode().equals("async")) {
			ConnectionPool pool = new AsyncConnectionPool(groupId);
			Global.poolList.put(groupId, pool);
		} else {
			logger.error("非法的连接池类型！");
			throw new CacheServiceException(CacheServiceErrorInfo.e12);
		}
	}

	public boolean hasGroupId(String groupId) {
		return this.globalGroupId.contains(groupId);
	}
	
	private void loadConfigInfo(String groupId) {
		String initUrl = String.format("%s/%s/%s?%s", MetasvrUrlConfig.get().getNextUrl(), 
				CONSTS.CACHE_SERVICE, CONSTS.FUN_GET_PROXY, "SERV_ID="+groupId);
		SVarObject sVarInvoke = new SVarObject();
		boolean retInvoke = HttpUtils.getData(initUrl, sVarInvoke);
		
		if (retInvoke) {
			JSONObject jsonObj = JSONObject.parseObject(sVarInvoke.getVal());
			if (jsonObj.getIntValue(CONSTS.JSON_HEADER_RET_CODE) == CONSTS.REVOKE_OK) {
				JSONArray array = jsonObj.getJSONArray(CONSTS.JSON_HEADER_RET_INFO);
				for (int i=0; i<array.size(); i++) {
					JSONObject o = array.getJSONObject(i);
					Proxy proxy = new Proxy(o, groupId);
					mapProxy.put(proxy.getID(), proxy);
				}
			} else {
				logger.error("缓存客户端初始化失败！"+jsonObj.get(CONSTS.JSON_HEADER_RET_INFO));
				throw new CacheServiceException(CacheServiceErrorInfo.e12);
			}
		} else {
			logger.error("缓存客户端初始化失败！");
			throw new CacheServiceException(CacheServiceErrorInfo.e12);
		}
	}
	
	public Map<String, Proxy> getMapProxy(String groupId) {
	    Map<String, Proxy> result = new HashMap<String, Proxy>();
	    for (String ID : this.mapProxy.keySet()) {
	    	if (this.mapProxy.get(ID).getGroupID().equals(groupId)) {
	    		result.put(ID, this.mapProxy.get(ID));
	    	}
	    }
	    return result;
	}
	
	@Override
	public void postEvent(JSONObject event) {
		int code = event.getInteger(CONSTS.EV_CODE);
		String jsonStr = event.getString(CONSTS.EV_JSON_STR);
		JSONObject obj = JSONObject.parseObject(jsonStr);
		
		switch (EventType.get(code)) {
		case e61:
			addProxy(obj.getString("SERV_NAME"), obj);
			break;
		case e62:
			removeProxy(obj.getString("SERV_NAME"), obj);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void doCompute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doReport() {
		String lsnrAddr = EventController.getInstance().getLsnrAddr();
		if (StringUtils.isNullOrEmtpy(lsnrAddr))
			return;
		
		BasicOperation.putClientStatisticInfo("cureuprapapa", lsnrAddr, CONSTS.TYPE_CACHE_CLIENT);
	}
	
	private void addProxy(String groupID, JSONObject obj) {
		if (this.hasGroupId(groupID)) {
			Proxy proxy = new Proxy(obj, groupID);
			mapProxy.put(proxy.getID(), proxy);
			Global.poolList.get(groupID).addProxy(proxy.getID());
		}
	}
	
	private void removeProxy(String groupID, JSONObject obj) {
		if (this.hasGroupId(groupID)) {
			Proxy proxy = new Proxy(obj, groupID);
			if (mapProxy.containsKey(proxy.getID())) {
				Global.poolList.get(groupID).decreaseProxy(proxy.getID());
				mapProxy.remove(proxy.getID());
			}
		}
	}

	public synchronized void close() {
		// unsubscribe and stop event controller
		EventController.getInstance().unsubscribe(CONSTS.TYPE_CACHE_CLIENT);
		EventController.getInstance().shutdown();
		
		MetasvrUrlConfig.get().close();
		
		for (ConnectionPool pool : Global.poolList.values()) {
			pool.shutdown();
		}
		if (mapProxy != null && mapProxy.size() != 0) {
			mapProxy.clear();
			mapProxy = null;
		}
		if (instance!=null) {
			instance = null;
		}
	}
	
}
