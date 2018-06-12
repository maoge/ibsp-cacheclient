package ibsp.cache.client.utils;

import java.util.HashMap;
import java.util.Map;

import ibsp.cache.client.pool.ConnectionPool;


public class Global {
	
	public static Map<String, ConnectionPool> poolList = new HashMap<String, ConnectionPool>();
	
}
