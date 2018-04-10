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
	public static final String JSON_HEADER_REMOTE_IP   = "REMOTE_IP";
	
	public static final int REVOKE_OK                  = 0;
	public static final int REVOKE_NOK                 = -1;
	
	public static final String META_SERVICE            = "metasvr";
	public static final String CACHE_SERVICE           = "cachesvr";
	
	public static final String FUN_URL_TEST            = "test";
	public static final String FUN_GET_PROXY           = "getDeployedProxyByServiceName";
	public static final String FUN_PUT_STATISTIC_INFO  = "putClientStatisticInfo";
	
	public static final int FIX_HEAD_LEN    = 10;
	public static final int FIX_PREHEAD_LEN = 6;
	public static final byte[] PRE_HEAD     = {'$','H','E','A','D',':'};
	
	//event listen and dispatch
	public static final int BASE_PORT = 9500;
	public static final int BATCH_FIND_CNT = 1000;
	public static final int GET_IP_RETRY = 5;
	public static final int GET_IP_RETRY_INTERVAL = 500;
	public static final int REPORT_INTERVAL        = 10000; // 定时上报间隔
	public static final int EVENT_DISPACH_INTERVAL = 10;    // 事件派发空闲休眠间隔
	public static final int RECONNECT_INTERVAL     = 1000;  // 重连间隔
	
	public static final String PARAM_CLIENT_TYPE = "CLIENT_TYPE";
	public static final String PARAM_LSNR_ADDR = "LSNR_ADDR";
	public static final String PARAM_CLIENT_INFO = "CLIENT_INFO";
	
	public static final String TYPE_CACHE_CLIENT = "CACHE_CLIENT";
	
	//event
	public static final String EV_CODE = "EVENT_CODE";
	public static final String EV_SERV_ID = "SERV_ID";
	public static final String EV_JSON_STR = "JSON_STR";
}
