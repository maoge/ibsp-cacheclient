package ibsp.cache.client.utils;

import java.nio.charset.Charset;

/***
 * 常量定义
 * @author 
 *
 */
public class CONSTS {

	public enum DataType {
		STRING, HASH, LIST;
	}
	
	public enum Command {
		SET, GET, STRLEN, APPEND, DECRBY, DECR, INCR, INCRBY, GETRANGE, GETSET, SETEX, SETNX, SETRANGE,
		EXISTS, TYPE, DEL, EXPIRE, RENAME, RENAMENX, PERSIST, TTL, PEXPIRE, PTTL,
		HGET, HSET, HEXISTS, HDEL, HKEYS, HVALS, HGETALL, HMSET, HLEN, HMGET, HSETNX, HINCRBY, HSCAN,
		LINDEX, LLEN, LPOP, LPUSH, LRANGE, LSET, LREM, LTRIM, RPOP, RPUSH, LINSERT, LPUSHX, RPUSHX,
		SADD, SCARD, SISMEMBER, SMEMBERS, SREM, 
		ZADD, ZCARD, ZCOUNT, ZINCRBY, ZRANGE, ZRANGEWITHSCORES, ZRANGEBYSCORE, ZRANGEBYSCOREWITHSCORES, ZREM, ZSCORE, ZREMRANGEBYRANK, ZREMRANGEBYSCORE;
	}
	
	public static final String DEFAULT_LOCK_PRE = "__GLOABAL__:__LOCK__PRE__:";

//	public static final String CONS_ZOOKEEPER_HOST = "zookeeper.host";
//	public static final String CONS_ZOOKEEPER_ROOT_PATH = "zookeeper.root.path";
    public static final String REDIS_PROXY_TIMEOUT = "redis.proxy.timeout";
	public static final String POOL_SIZE= "pool.size";
	public static final String CONNECTION_MODE = "connection.mode";
	public static final String CONS_SERVICE_NAME = "service.name";
	public static final String CONS_METASVR_ROOTURL = "metasvr.rooturl";
	
	public static final Charset CHARSET = Charset.forName("UTF-8");
	public static final int MAX_REDIS_BODY = 1024 * 1024 * 1024;
	public static final String CONF_PATH = "conf";
	
	public static final String HTTP_PROTOCAL           = "http";
	public static final String HTTP_METHOD_GET         = "GET";
	public static final String HTTP_METHOD_POST        = "POST";
	
	public static final String JSON_HEADER_RET_CODE    = "RET_CODE";
	public static final String JSON_HEADER_RET_INFO    = "RET_INFO";
	
	public static final int REVOKE_OK                  = 0;
	public static final int REVOKE_NOK                 = -1;
	
	public static final String META_SERVICE            = "metasvr";
	public static final String CACHE_SERVICE           = "cachesvr";
	
	public static final String FUN_URL_TEST            = "test";
	public static final String FUN_GET_PROXY           = "getProxyByServiceName";
	
}
