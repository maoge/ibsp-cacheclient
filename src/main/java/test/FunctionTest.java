package test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.core.CacheService;
import ibsp.cache.client.protocol.Tuple;

public class FunctionTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FunctionTest.class);
    private CacheService cacheService;

    public FunctionTest() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream("conf/test.properties"));
        Properties p = new Properties();
        p.load(in);
        this.cacheService = CacheService.getInstance();
    }
    
	public void run() {
		try {
			PropertyConfigurator.configure("conf/log4j.properties");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            Date date = new Date(System.currentTimeMillis());
            logger.info("--------------------------------------------------------------------------");
            logger.info("FunctionTest start at: "+format.format(date));
            TC_001();
            TC_002();
            TC_004();
            TC_005();
            TC_0051();
            TC_006();
            
            date = new Date(System.currentTimeMillis());
            logger.info("FunctionTest end at: "+format.format(date));
            logger.info("--------------------------------------------------------------------------");
            cacheService.close();
        } catch (Exception e) {
            logger.info("测试失败："+e.getMessage());
        }
	}

    public void TC_001() {
        String Key = "foo", Value = "bar";      
        /** SET **/
        try {
            String status = cacheService.set(Key, Value);
            if (status.equals("0")) {
                logger.info("set测试通过！");
            } else {
                logger.info("set测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("set测试失败："+e.getMessage());
        }
        /** GET **/
        try {
            String value = cacheService.get(Key);
            if (value.equals(Value)) {
                logger.info("get测试通过！");
            } else {
                logger.info("get测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("get测试失败："+e.getMessage());
        }
        /** APPEND **/
        try {
            long result = cacheService.append(Key, "123456");
            if (result==9) {
                logger.info("append测试通过！");
            } else {
                logger.info("append测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("append测试失败："+e.getMessage());
        }
        /** SETNX **/
        try {
            long result = cacheService.setnx(Key, "123456");
            if (result==0) {
                logger.info("setnx测试通过！");
            } else {
                logger.info("setnx测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("setnx测试失败："+e.getMessage());
        }
        /** SETEX **/
        try {
            String status = cacheService.setex(Key, 30, "123456");
            if (status.equals("0")) {
                logger.info("setex测试通过！");
            } else {
                logger.info("setex测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("setex测试失败："+e.getMessage());
        }
        /** GETSET **/
        try {
            byte[] byteValue = cacheService.getSet(Key, "abcdefg");
            if (new String(byteValue).equals("123456")) {
                logger.info("getSet测试通过！");
            } else {
                logger.info("getSet测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("getSet测试失败："+e.getMessage());
        }
        /** SETRANGE **/
        try {
            long result = cacheService.setrange(Key, 0, "9876543210");
            if (result==10) {
                logger.info("setrange测试通过！");
            } else {
                logger.info("setrange测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("setrange测试失败："+e.getMessage());
        }
        /** GETRANGE **/
        try {
            byte[] byteValue = cacheService.getrange(Key, 5, 10);
            if (new String(byteValue).equals("43210")) {
                logger.info("getrange测试通过！");
            } else {
                logger.info("getrange测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("getrange测试失败："+e.getMessage());
        }
        /** STRLEN **/
        try {
            long result = cacheService.strlen(Key);
            if (result==10) {
                logger.info("strlen测试通过！");
            } else {
                logger.info("strlen测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("strlen测试失败："+e.getMessage());
        }
        /** INCR **/
        try {
            cacheService.set("zoo", "0");
            long result = cacheService.incr("zoo");
            if (result==1) {
                logger.info("incr测试通过！");
            } else {
                logger.info("incr测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("incr测试失败："+e.getMessage());
        }
        /** DECR **/
        try {
            long result = cacheService.decr("zoo");
            if (result==0) {
                logger.info("decr测试通过！");
            } else {
                logger.info("decr测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("decr测试失败："+e.getMessage());
        }
        /** INCRBY **/
        try {
            long result = cacheService.incrBy("zoo", 10);
            if (result==10) {
                logger.info("incrBy测试通过！");
            } else {
                logger.info("incrBy测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("incrBy测试失败："+e.getMessage());
        }
        /** DECRBY **/
        try {
            long result = cacheService.decrBy("zoo", 5);
            if (result==5) {
                logger.info("decrBy测试通过！");
            } else {
                logger.info("decrBy测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("decrBy测试失败："+e.getMessage());
        }
        /** EXISTS **/
        try {
            boolean bResult = cacheService.exists("test");
            if (!bResult) {
                logger.info("exists测试通过！");
            } else {
                logger.info("exists测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("exists测试失败："+e.getMessage());
        }
        /** TYPE **/
        try {
            String value = cacheService.type("zoo");
            if (value.equals("string")) {
                logger.info("type测试通过！");
            } else {
                logger.info("type测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("type测试失败："+e.getMessage());
        }
        /** EXPIRE **/
        try {
            long result = cacheService.expire(Key, 60);
            if (result==1) {
                logger.info("expire测试通过！");
            } else {
                logger.info("expire测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("expire测试失败："+e.getMessage());
        }
        try {
            Thread.sleep( 3000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /** TTL **/
        try {
            long result = cacheService.ttl(Key);
            if (result==57) {
                logger.info("ttl测试通过！");
            } else {
                logger.info("ttl测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("ttl测试失败："+e.getMessage());
        }
        /** PEXPIRE **/
        try {
            long result = cacheService.pexpire(Key, 5000);
            if (result==1) {
                logger.info("pexpire测试通过！");
            } else {
                logger.info("pexpire测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("pexpire测试失败："+e.getMessage());
        }
        try {
            Thread.sleep( 3000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /** PTTL **/
        try {
            long result = cacheService.pttl(Key);
            if (result<3000 && result>1000) {
                logger.info("pttl测试通过！");
            } else {
                logger.info("pttl测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("pttl测试失败："+e.getMessage());
        }
        /** PERSIST **/
        try {
            long result = cacheService.persist(Key);
            if (result==1) {
                logger.info("persist测试通过！");
            } else {
                logger.info("persist测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("persist测试失败："+e.getMessage());
        }
        /** STRLEN **/
        try {
            long result = cacheService.strlen(Key);
            if (result==10) {
                logger.info("strlen测试通过！");
            } else {
                logger.info("strlen测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("strlen测试失败："+e.getMessage());
        }
        /** DEL **/
        try {
            long flag = cacheService.del(Key);
            if (flag==1) {
                logger.info("del测试通过！");
            } else {
                logger.info("del测试失败！返回值错误。");
            }      
        } catch (Exception e) {
            logger.info("del测试失败："+e.getMessage());
        }
    }

    public void TC_002() {
        String hashKey = "hash001";
        String field1 = "test1", field2 = "test2", field3 = "test3", field4 = "test4", field5 = "test5";
        String value1 = "value1", value2 = "value2", value3 = "value3", value4 = "value4";
        Map< String, String> map = new HashMap<>();
        map.put(field1, value1);
        map.put(field2, value2);
        map.put(field3, value3);
        cacheService.del(hashKey );
        /** HMSET **/
        try {
            String result = cacheService.hmset(hashKey, map);
            if (result.equals("OK")) {
                logger.info("hmset测试通过！");
            } else {
                logger.info("hmset测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hmset测试失败："+e.getMessage());
        }
        /** HSETNX **/
        try {
            long flag = cacheService.hsetnx(hashKey, field4, value4);
            if (flag==1) {
                logger.info("hsetnx测试通过！");
            } else {
                logger.info("hsetnx测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hsetnx测试失败："+e.getMessage());
        }
        /** HSET **/  
        try {
            long flag = cacheService.hset(hashKey, field1, "+value1");
            if (flag==0) {
                logger.info("hset测试通过！");
            } else {
                logger.info("hset测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hset测试失败："+e.getMessage());
        }
        /** HGET **/    
        try {
            String result = cacheService.hget(hashKey, field1);
            if (result.equals("+value1")) {
                logger.info("hget测试通过！");
            } else {
                logger.info("hget测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hget测试失败："+e.getMessage());
        }
        /** HMGET **/
        try {
            List<String> lstResult = cacheService.hmget(hashKey, field1, field2, field3, field4);
            if (lstResult.size()==4) {
                logger.info("hmget测试通过！");
            } else {
                logger.info("hmget测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hmget测试失败："+e.getMessage());
        }
        try {
            String[] temp = new String[]{field1, field2, field3, field4};
            List<byte[]> lstResult2 = cacheService.hmgetBytes(hashKey, temp);
            if (lstResult2.size()==4) {
                logger.info("hmgetBytes测试通过！");
            } else {
                logger.info("hmgetBytes测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hmgetBytes测试失败："+e.getMessage());
        }
        /** HMGETALL **/
        try {
            Map<String, String> mapResult = cacheService.hgetAll(hashKey);
            if (mapResult.size()==4) {
                logger.info("hgetAll测试通过！");
            } else {
                logger.info("hgetAll测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hgetAll测试失败："+e.getMessage());
        }
        /** HKEYS **/
        try {
            Set<String> setResult = cacheService.hkeys(hashKey);
            if (setResult.size()==4) {
                logger.info("hkeys测试通过！");
            } else {
                logger.info("hkeys测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hkeys测试失败："+e.getMessage());
        }
        /** HVALS **/
        try {
            List<byte[]> byteResult = cacheService.hvals(hashKey);
            if (byteResult.size()==4) {
                logger.info("hvals测试通过！");
            } else {
                logger.info("hvals测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hvals测试失败："+e.getMessage());
        }
        /** HINCRBY **/
        try {
            long flag = cacheService.hincrBy(hashKey, field5, 5);
            if (flag==5) {
                logger.info("hincrBy测试通过！");
            } else {
                logger.info("hincrBy测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hincrBy测试失败："+e.getMessage());
        }
        /** HEXISTS **/
        try {
            boolean bResult = cacheService.hexists(hashKey, field5);
            if (bResult) {
                logger.info("hexists测试通过！");
            } else {
                logger.info("hexists测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hexists测试失败："+e.getMessage());
        }
        /** HLEN **/    
        try {
            long flag = cacheService.hlen(hashKey);
            if (flag==5) {
                logger.info("hlen测试通过！");
            } else {
                logger.info("hlen测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("hlen测试失败："+e.getMessage());
        }
        /** HDEL **/  
        try {
            long flag = cacheService.hdel(hashKey, field1, field2);
            if (flag==2) {
                logger.info("hdel测试通过！");
            } else {
                logger.info("hdel测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("getrange测试失败："+e.getMessage());
        }
    }

    public void TC_004() {
        String listKey = "list001";
        String value1 = "value1", value2 = "value2", value3 = "value3", value4 = "value4", value5 = "0", value6 = "0", value7 = "0", value8 = "0";
        cacheService.del(listKey );
        /** LPUSH **/
        try {
            long result = cacheService.lpush(listKey, value1.getBytes(), value2.getBytes(), value3.getBytes(), value4.getBytes());
            if (result==4) {
                logger.info("lpush测试通过！");
            } else {
                logger.info("lpush测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lpush测试失败："+e.getMessage());
        }
        /** RPUSH **/
        try {
            long result = cacheService.rpush(listKey, value5.getBytes());
            if (result==5) {
                logger.info("rpush测试通过！");
            } else {
                logger.info("rpush测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("rpush测试失败："+e.getMessage());
        }
        /** LPUSHX **/ 
        try {
            long result = cacheService.lpushx(listKey, value6.getBytes());
            if (result==6) {
                logger.info("lpushx测试通过！");
            } else {
                logger.info("lpushx测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lpushx测试失败："+e.getMessage());
        }
        /** RPUSHX **/    
        try {
            long result = cacheService.rpushx(listKey, value7);
            if (result==7) {
                logger.info("rpushx测试通过！");
            } else {
                logger.info("rpushx测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("rpushx测试失败："+e.getMessage());
        }
        /** LPOP **/
        try {
            byte[] value = cacheService.lpop(listKey);
            if (value!=null) {
                logger.info("lpop测试通过！");
            } else {
                logger.info("lpop测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lpop测试失败："+e.getMessage());
        }
        /** RPOP **/   
        try {
            byte[] value = cacheService.rpop(listKey);
            if (value!=null) {
                logger.info("rpop测试通过！");
            } else {
                logger.info("rpop测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("rpop测试失败："+e.getMessage());
        }
        /** LINSERT **/    
        try {
            long result = cacheService.linsert(listKey, false, value6, value8);
            if (result==6) {
                logger.info("linsert测试通过！");
            } else {
                logger.info("linsert测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("linsert测试失败："+e.getMessage());
        }
        /** LLEN **/ 
        try {
            long result = cacheService.llen(listKey);
            if (result==6) {
                logger.info("llen测试通过！");
            } else {
                logger.info("llen测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("llen测试失败："+e.getMessage());
        }
        /** LRANGE **/
        try {
            List<byte[]> lstResult = cacheService.lrange(listKey, 1, 4);
            if (lstResult.size()==4) {
                logger.info("lrange测试通过！");
            } else {
                logger.info("lrange测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lrange测试失败："+e.getMessage());
        }
        /** LTRIM **/
        try {
            String status = cacheService.ltrim(listKey, 0, 5);
            if (status.equals("OK")) {
                logger.info("ltrim测试通过！");
            } else {
                logger.info("ltrim测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("ltrim测试失败："+e.getMessage());
        }
        /** LINDEX **/
        try {
            String value = cacheService.lindex(listKey, 0);
            if (value!=null) {
                logger.info("lindex测试通过！");
            } else {
                logger.info("lindex测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lindex测试失败："+e.getMessage());
        }
        /** LSET **/    
        try {
            String status = cacheService.lset(listKey, 0, value1);
            if (status.equals("OK")) {
                logger.info("lset测试通过！");
            } else {
                logger.info("lset测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lset测试失败："+e.getMessage());
        }
        try {
            long result = cacheService.lrem(listKey, 0, value1);
            if (result==2) {
                logger.info("lrem测试通过！");
            } else {
                logger.info("lrem测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("lrem测试失败："+e.getMessage());
        }
    }
    
    public void TC_005() {
        String setKey = "set001";
        String value1 = "value1", value2 = "value2", value3 = "value3";
        cacheService.del(setKey);
        /** SADD **/
        try {
            long result = cacheService.sadd(setKey, value1, value2, value3);
            if (result==3) {
                logger.info("sadd测试通过！");
            } else {
                logger.info("sadd测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("sadd测试失败："+e.getMessage());
        }
        /** SCARD **/
        try {
            long result = cacheService.scard(setKey);
            if (result==3) {
                logger.info("scard测试通过！");
            } else {
                logger.info("scard测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("scard测试失败："+e.getMessage());
        }
        /** SISMEMBER **/
        try {
            boolean bResult = cacheService.sismember(setKey, value3);
            if (bResult) {
                logger.info("sismember测试通过！");
            } else {
                logger.info("sismember测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("sismember测试失败："+e.getMessage());
        }
        /** SMEMBER **/
        try {
            Set<byte[]> setResult = cacheService.smembers(setKey);
            if (setResult.size()==3) {
                logger.info("smembers测试通过！");
            } else {
                logger.info("smembers测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("smembers测试失败："+e.getMessage());
        }
        /** SREM **/
        try {
            long result = cacheService.srem(setKey, value3);
            if (result==1) {
                logger.info("srem测试通过！");
            } else {
                logger.info("srem测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("srem测试失败："+e.getMessage());
        }
    }
    
    public void TC_0051() {
        String zSetKey = "zset001";
        String field1 = "test1", field2 = "test2", field3 = "test3", field4 = "test4", field5 = "test5";
        Double value1 = 1d, value2 = 2d, value3 = 3d, value4 = 4d, value5 = 10d;    
        Map< String, Double> zmap = new HashMap<>();
        zmap.put(field1, value1);
        zmap.put(field2, value2);
        zmap.put(field3, value3);
        zmap.put(field4, value4);
        zmap.put(field5, value5);
        cacheService.del(zSetKey);
        /** ZADD **/
        try {
            long result = cacheService.zadd(zSetKey, zmap);
            long result2 = cacheService.zadd(zSetKey, value4, field4);
            if (result==5 && result2==0) {
                logger.info("zadd测试通过！");
            } else {
                logger.info("zadd测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zadd测试失败："+e.getMessage());
        }
        /** ZCARD **/
        try {
            long result = cacheService.zcard(zSetKey);
            if (result==5) {
                logger.info("zcard测试通过！");
            } else {
                logger.info("zcard测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zcard测试失败："+e.getMessage());
        }
        /** ZSCORE **/
        try {
            double dResult = cacheService.zscore(zSetKey, field1);
            if (dResult<1.1 && dResult>0.9) {
                logger.info("zscore测试通过！");
            } else {
                logger.info("zscore测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zscore测试失败："+e.getMessage());
        }
        /** ZCOUNT **/
        try {
            long result = cacheService.zcount(zSetKey, 0, 5);
            if (result==4) {
                logger.info("zcount测试通过！");
            } else {
                logger.info("zcount测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zcount测试失败："+e.getMessage());
        }
        /** ZRANGE **/   
        try {
            Set<String> setResult = cacheService.zrange(zSetKey, 0, -1);
            if (setResult.size()==5) {
                logger.info("zrange测试通过！");
            } else {
                logger.info("zrange测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zrange测试失败："+e.getMessage());
        }
        /** ZRANGEBYSCORE **/
        try {
            Set<String> setResult = cacheService.zrangeByScore(zSetKey, 0, 900);
            if (setResult.size()==5) {
                logger.info("zrangeByScore测试通过！");
            } else {
                logger.info("zrangeByScore测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zrangeByScore测试失败："+e.getMessage());
        }
        try {
            Set<Tuple> tResult = cacheService.zrangeByScoreWithScores(zSetKey, 3d, 10d);
            if (tResult.size()==3) {
                logger.info("zrangeByScoreWithScores测试通过！");
            } else {
                logger.info("zrangeByScoreWithScores测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zrangeByScoreWithScores测试失败："+e.getMessage());
        }
        /** ZRANGEBYSCORE **/
        try {
            long result = cacheService.zremrangeByRank(zSetKey, 0, 0);
            if (result==1) {
                logger.info("zremrangeByRank测试通过！");
            } else {
                logger.info("zremrangeByRank测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zremrangeByRank测试失败："+e.getMessage());
        }
        /** ZRANGEBYSCORE **/
        try {
            long result = cacheService.zremrangeByScore(zSetKey, 2, 4);
            if (result==3) {
                logger.info("zremrangeByScore测试通过！");
            } else {
                logger.info("zremrangeByScore测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zremrangeByScore测试失败："+e.getMessage());
        }
        /** ZRANGEBYSCORE **/
        try {
            double dResult = cacheService.zincrby(zSetKey, 3.14159262789, field2);
            if (dResult<3.15 && dResult>3.14) {
                logger.info("zincrby测试通过！");
            } else {
                logger.info("zincrby测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zincrby测试失败："+e.getMessage());
        }
        /** ZREM **/
        try {
            long result = cacheService.zrem(zSetKey, field1);
            if (result==0) {
                logger.info("zrem测试通过！");
            } else {
                logger.info("zrem测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("zrem测试失败："+e.getMessage());
        }
    }

    public void TC_006() {
        String Key = "foo";
        byte[] value = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            if (i % 3 == 0) {
                value[i] = 'p';
            } else if (i % 3 == 1) {
                value[i] = 'o';
            } else {
                value[i] = 'i';
            }
        }               
        /** SET **/
        try {
            String status = cacheService.set(Key, value);
            if (status.equals("0")) {
                logger.info("set(byte)测试通过！");
            } else {
                logger.info("set(byte)测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("set(byte)测试失败："+e.getMessage());
        }
        /** GET **/
        try {
            byte[] value2 = cacheService.getBytes(Key);
            if (value2.length==1024) {
                logger.info("getBytes测试通过！");
            } else {
                logger.info("getBytes测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("getBytes测试失败："+e.getMessage());
        }
        /** SETNX **/
        try {
            long result = cacheService.setnx(Key, value);
            if (result==0) {
                logger.info("setnx(byte)测试通过！");
            } else {
                logger.info("setnx(byte)测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("setnx(byte)测试失败："+e.getMessage());
        }
        /** SETEX **/
        try {
            String status = cacheService.setex(Key, 30, "abcdefg中文绰号测试".getBytes());
            if (status.equals("0")) {
                logger.info("setex测试通过！");
            } else {
                logger.info("setex测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("setex(byte)测试失败："+e.getMessage());
        }
        /** GETSET **/
        try {
            byte[] value3 = cacheService.getSet(Key, value);
            if (new String(value3).equals("abcdefg中文绰号测试")) {
                logger.info("getSet(byte)测试通过！");
            } else {
                logger.info("getSet(byte)测试失败！返回值错误。");
            }
        } catch (Exception e) {
            logger.info("getSet(byte)测试失败："+e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
    	new FunctionTest().run();
    	Thread.sleep(100);
    }
}
