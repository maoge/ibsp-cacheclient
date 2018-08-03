package ibsp.cache.client.pool;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.config.MetasvrConfigFactory;
import ibsp.cache.client.config.Proxy;
import ibsp.cache.client.core.NJedis;
import ibsp.cache.client.core.Nheader;
import ibsp.cache.client.exception.CacheServiceException;
import ibsp.cache.client.exception.ProxyRedisException;
import ibsp.cache.client.exception.CacheServiceException.CacheServiceErrorInfo;
import ibsp.cache.client.structure.CacheRequest;
import ibsp.cache.client.structure.CacheResponse;
import ibsp.cache.client.structure.Operate;
import ibsp.common.utils.IBSPConfig;
import ibsp.cache.client.exception.RedisConnectionException;
import ibsp.cache.client.exception.RedisDataException;
import ibsp.cache.client.exception.RedisException;

public abstract class ConnectionPool {
	public static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

	private final int size;

	private MetasvrConfigFactory configs;
	private Map<String, Integer> connectionCount;
	private String groupID;

	protected AtomicLong count;
	protected volatile boolean shuttingDown;
	protected List<AtomicReference<Poolable<NJedis>>> poolList;
	protected List<AtomicReference<Poolable<NJedis>>> errorList;
	protected final ReentrantLock updateLock = new ReentrantLock();

	protected Map<Exception, String> excepts = Collections.synchronizedMap(new HashMap<Exception, String>());

	protected ConnectionPool(String groupID) {
		this.size = IBSPConfig.getInstance().getCachePoolSize();
		this.count = new AtomicLong(0);
		this.groupID = groupID;
		this.poolList = Collections.synchronizedList(new ArrayList<AtomicReference<Poolable<NJedis>>>());
		this.errorList = Collections.synchronizedList(new ArrayList<AtomicReference<Poolable<NJedis>>>());

		this.configs = MetasvrConfigFactory.getInstance();
		Map<String, Proxy> proxyMap = configs.getMapProxy(groupID);
		connectionCount = new HashMap<String, Integer>();

		updateLock.lock();
		try {
			if (proxyMap == null && proxyMap.size() == 0) {
				logger.error("proxy map is null!");
				return;
			}

			Set<String> proxyKeys = proxyMap.keySet();
			for (int i = 0; i < size; i++) {
				for (String key : proxyKeys) {
					if (this.connectionCount.containsKey(key)) {
						this.connectionCount.put(key, this.connectionCount.get(key) + 1);
					} else {
						this.connectionCount.put(key, 1);
					}

					String name = String.format("Connection%d_%s", i, key);
					Proxy proxy = proxyMap.get(key);
					String[] temp = proxy.getAddress().split(":");
					makeNewConnection(name, temp[0], Integer.parseInt(temp[1]));
				}
			}

		} finally {
			updateLock.unlock();
		}
	}

	/**
	 * 从连接池中取出一个连接对象
	 */
	public abstract Poolable<NJedis> borrowObject();

	/**
	 * 关闭连接池 1. 关闭checker和reconnector线程 2. 关闭连接池中的所有连接 3. 清除异常列表、连接列表
	 */
	public synchronized void shutdown() {
		updateLock.lock();
		shuttingDown = true;

		for (AtomicReference<Poolable<NJedis>> obj : poolList) {
			logger.debug("shutdown obj=" + obj.get().getName());
			decreaseObject(obj.get());
		}
		this.excepts.clear();
		errorList.clear();
		poolList.clear();
		updateLock.unlock();
	}

	/**
	 * 关闭一个连接
	 * 
	 * @param obj
	 */
	public void decreaseObject(Poolable<NJedis> obj) {
		NJedis jedis = obj.getObject();
		try {
			jedis.close();
		} catch (Exception e) {
			logger.warn("Destroy connection failed!", e);
		}
	}

	/**
	 * 选择一个可用的连接发送请求，处理各类异常情况
	 * 
	 * @param request
	 * @return
	 * @throws CacheServiceException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
	public CacheResponse call(CacheRequest<? extends Operate> request) throws CacheServiceException {

		CacheResponse response = null;
		long respLength = 0L;
		Object result = null;
		CacheServiceException cacheServiceException = null;

		Poolable<NJedis> poolObject = borrowObject();
		NJedis accessConn = poolObject == null ? null : poolObject.getObject();

		if (accessConn != null && accessConn.isConnected()) {
			try {
				accessConn.setCurrentHeader(new Nheader(request.getGroupId()));
				result = request.getParam().doExecute(accessConn);
				if (result == null) {
					respLength = 0;
				} else {
					respLength = request.getParam().getRespLength();
				}
			} catch (Exception e) {
				logger.error("执行redis请求时出错! Cause:" + e.toString(), e);
				result = null;
				cacheServiceException = new CacheServiceException(CacheServiceErrorInfo.DEFAULT);
				if (e instanceof RedisDataException) {
					String errorCode = e.getMessage();
					if (errorCode.startsWith("errorCode")) {
						errorCode = errorCode.substring(errorCode.indexOf(":") + 1, errorCode.length());
						cacheServiceException = new CacheServiceException(errorCode, "接入机返回错误!");
						cacheServiceException.setConnName(poolObject.getObject().getName());
					} else {
						cacheServiceException = new CacheServiceException(CacheServiceErrorInfo.e5);
						cacheServiceException.setConnName(poolObject.getObject().getName());
						throw cacheServiceException;
					}
				} else if (e instanceof RedisConnectionException) {
					cacheServiceException = new CacheServiceException(CacheServiceErrorInfo.e1);
					cacheServiceException.setConnName(poolObject.getObject().getName());
					throw cacheServiceException;
				} else if (e instanceof RedisException) {
					String errorMsg = e.getMessage();
					if (errorMsg.indexOf("timeout") != -1) {
						cacheServiceException = new CacheServiceException(CacheServiceErrorInfo.e4);
						cacheServiceException.setConnName(poolObject.getObject().getName());
						throw cacheServiceException;
					} else {
						cacheServiceException = new CacheServiceException(errorMsg);
						cacheServiceException.setConnName(poolObject.getObject().getName());
					}
				}
				throw cacheServiceException;
			} finally {
				returnObject(poolObject);
			}
			response = CacheResponse.okOf(request.getRequestId(), result, respLength);

		} else {
			logger.error("连接池中不存在可用的连接!");
			response = CacheResponse.errorOf(request.getRequestId(), "连接池中不存在可用的连接!");
			cacheServiceException = new CacheServiceException(CacheServiceErrorInfo.e2);
			if (poolObject != null) {
				returnObject(poolObject);
			}
			throw cacheServiceException;
		}

		return response;
	}

	/**
	 * 将连接归还到连接池中
	 * 
	 * @param freeObject
	 */
	public abstract void returnObject(Poolable<NJedis> freeObject);

	/**
	 * 加入一个新的接入机，从原有接入机挪用一部分连接给新的接入机
	 * 
	 * @param proxyName
	 */
	public void addProxy(String proxyName) {
		updateLock.lock();
		try {
			connectionCount.put(proxyName, 0);
			Map<String, Proxy> proxyMap = configs.getMapProxy(this.groupID);
			String[] address = proxyMap.get(proxyName).getAddress().split(":");

			while (true) {
				String maxProxy = "";
				int maxConnCount = 0;
				int minConnCount = Integer.MAX_VALUE;
				RandomString strings = new RandomString(connectionCount.keySet());
				for (int i = 0; i < connectionCount.keySet().size(); i++) {
					String pName = strings.nextString();
					if (!pName.equals(proxyName)) {
						if (connectionCount.get(pName) > maxConnCount) {
							maxConnCount = connectionCount.get(pName);
							maxProxy = pName;
						}
						if (connectionCount.get(pName) < minConnCount) {
							minConnCount = connectionCount.get(pName);
						}
					}
				}
				minConnCount = minConnCount > size ? size : minConnCount;
				if (connectionCount.get(proxyName) >= minConnCount) {
					break;
				}

				String newName = "";
				if (!maxProxy.equals("")) {
					connectionCount.put(maxProxy, connectionCount.get(maxProxy) - 1);
					AtomicReference<Poolable<NJedis>> disconnect = null;
					for (AtomicReference<Poolable<NJedis>> reference : poolList) {
						if (reference.get().getObject().getName().contains(maxProxy)) {
							disconnect = reference;
							poolList.remove(reference);
							break;
						}
					}

					if (disconnect == null) {
						for (AtomicReference<Poolable<NJedis>> reference : errorList) {
							if (reference.get().getObject().getName().contains(maxProxy)) {
								disconnect = reference;
								errorList.remove(reference);
								break;
							}
						}
					}

					if (disconnect == null) {
						throw new RuntimeException("No available connection for disconnect!");
					}

					logger.info("Disconnect: " + disconnect.get().getObject().getName());
					Thread.sleep(100);
					newName = disconnect.get().getObject().getName().split("_")[0] + "_" + proxyName;
					disconnect.get().getObject().close();
				} else {
					newName = "Connection" + (connectionCount.get(proxyName) + 1) + "_" + proxyName;
				}

				connectionCount.put(proxyName, connectionCount.get(proxyName) + 1);
				makeNewConnection(newName, address[0], Integer.parseInt(address[1]));
			}

		} catch (Exception e) {
			logger.error("新增接入机连接失败！", e);
		} finally {
			updateLock.unlock();
		}
	}

	/**
	 * 减少一个接入机，将该接入机的连接分给其他接入机
	 * 
	 * @param proxyName
	 */
	public void decreaseProxy(String proxyName) {
		updateLock.lock();
		try {
			while (connectionCount.get(proxyName) > 0) {
				String minProxy = "";
				int minConnCount = Integer.MAX_VALUE;
				RandomString strings = new RandomString(connectionCount.keySet());
				for (int i = 0; i < connectionCount.keySet().size(); i++) {
					String pName = strings.nextString();
					if (!pName.equals(proxyName)) {
						if (connectionCount.get(pName) < minConnCount) {
							minConnCount = connectionCount.get(pName);
							minProxy = pName;
						}
					}
				}

				connectionCount.put(proxyName, connectionCount.get(proxyName) - 1);
				AtomicReference<Poolable<NJedis>> disconnect = null;

				for (AtomicReference<Poolable<NJedis>> reference : poolList) {
					if (reference.get().getObject().getName().contains(proxyName)) {
						disconnect = reference;
						poolList.remove(reference);
						break;
					}
				}

				if (disconnect == null) {
					for (AtomicReference<Poolable<NJedis>> reference : errorList) {
						if (reference.get().getObject().getName().contains(proxyName)) {
							disconnect = reference;
							errorList.remove(reference);
							break;
						}
					}
				}

				if (disconnect == null) {
					throw new RuntimeException("No available connection for disconnect!");
				}

				logger.info("Disconnect: " + disconnect.get().getObject().getName());
				Thread.sleep(100);
				String newName = disconnect.get().getObject().getName().split("_")[0] + "_" + minProxy;
				disconnect.get().getObject().close();

				if (minProxy != null && !minProxy.equals("")) {
					connectionCount.put(minProxy, connectionCount.get(minProxy) + 1);
					Map<String, Proxy> proxyMap = configs.getMapProxy(this.groupID);
					String[] address = proxyMap.get(minProxy).getAddress().split(":");
					makeNewConnection(newName, address[0], Integer.parseInt(address[1]));
				}
			}

			connectionCount.remove(proxyName);

		} catch (Exception e) {
			logger.error("移除接入机连接失败！", e);
		} finally {
			updateLock.unlock();
		}
	}

	/**
	 * 建立一个新的连接
	 * 
	 * @param name
	 * @param ip
	 * @param port
	 */
	public void makeNewConnection(String name, String ip, int port) {
		AtomicReference<Poolable<NJedis>> poolable = new AtomicReference<Poolable<NJedis>>();
		logger.info("Make connection: " + name);
		try {
			NJedis jedis = null;
			String mode = IBSPConfig.getInstance().getCacheConnectionMode();
			int timeout = IBSPConfig.getInstance().getCacheRedisProxyTimeout();
			if (mode.equals("async")) {
				jedis = new NJedis(ip, port, timeout, name, false);
			} else if (mode.equals("sync")) {
				jedis = new NJedis(ip, port, timeout, name, true);
			}
			if (jedis != null) {
				jedis.setExceptionList(this.excepts);
				jedis.reconnect();
				poolable.set(new Poolable<NJedis>(jedis));
				poolList.add(poolable);
			} else {
				logger.error("Invalid connection mode! Failed to make connection!");
			}
		} catch (Exception e) {
			synchronized (this.excepts) {
				this.excepts.put(e, name);
			}
			errorList.add(poolable);
		}
	}

	/**
	 * 检查异常列表并重连连接
	 */
	protected boolean checkException() {

		synchronized (this.excepts) {
			for (Exception e : this.excepts.keySet()) {
				if (((e instanceof SocketException)) || ((e instanceof SocketTimeoutException))
						|| ((e instanceof ConnectException)) || ((e instanceof ProxyRedisException))
						|| ((e instanceof IOException)) || ((e instanceof RuntimeException))) {
					putToErrorList(this.excepts.get(e));
				}
			}
			this.excepts.clear();
		}

		List<AtomicReference<Poolable<NJedis>>> tempList = new ArrayList<AtomicReference<Poolable<NJedis>>>();
		for (int i = 0; i < errorList.size(); i++) {
			AtomicReference<Poolable<NJedis>> reference = errorList.get(i);
			logger.warn("正在重连" + reference.get().getObject().getName());
			boolean result = reference.get().getObject().reconnect();
			if (result) {
				tempList.add(reference);
			}
		}

		for (AtomicReference<Poolable<NJedis>> reference : tempList) {
			updateLock.lock();
			try {
				errorList.remove(reference);
				poolList.add(reference);
			} catch (Exception e) {
				logger.error("Recover connection " + reference.get().getObject().getName() + " failed!", e);
			} finally {
				updateLock.unlock();
			}
		}

		if (tempList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据连接名将连接放入错误列表中
	 */
	protected void putToErrorList(String connName) {
		updateLock.lock();

		try {
			for (AtomicReference<Poolable<NJedis>> reference : errorList) {
				String name = reference.get().getObject().getName();
				if (name.equals(connName)) {
					return;
				}
			}

			AtomicReference<Poolable<NJedis>> moving = null;

			for (AtomicReference<Poolable<NJedis>> reference : poolList) {
				String name = reference.get().getObject().getName();
				if (name.equals(connName)) {
					moving = reference;
					break;
				}
			}

			if (moving != null) {
				errorList.add(moving);
				poolList.remove(moving);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			updateLock.unlock();
		}
	}

	/**
	 * 从一个字符串的Set中不重复地随机取值
	 */
	private class RandomString {

		private String[] list;
		private boolean[] hit;
		private int hitCount = 0;
		private Random r = new Random();

		public RandomString(Set<String> strings) {
			this.list = new String[strings.size()];
			int k = 0;
			for (String string : strings) {
				list[k++] = string;
			}
			this.hit = new boolean[list.length];
			for (int i = 0; i < list.length; i++) {
				hit[i] = false;
			}
		}

		public String nextString() {
			if (list.length == 0)
				return "";

			if (hitCount == list.length) {
				for (int i = 0; i < list.length; i++) {
					hit[i] = false;
				}
				hitCount = 0;
			}

			boolean ok = false;
			String result = "";
			while (!ok) {
				int key = r.nextInt(list.length);
				if (hit[key])
					continue;
				hit[key] = true;
				hitCount++;
				result = list[key];
				ok = true;
			}
			return result;
		}
	}

}
