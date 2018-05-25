package ibsp.cache.client.core;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ibsp.cache.client.config.Configuration;
import ibsp.cache.client.protocol.ByteUtil;
import ibsp.cache.client.protocol.ScanParams;
import ibsp.cache.client.protocol.ScanResult;
import ibsp.cache.client.structure.Append;
import ibsp.cache.client.structure.CacheRequest;
import ibsp.cache.client.structure.CacheResponse;
import ibsp.cache.client.structure.Decr;
import ibsp.cache.client.structure.Decrby;
import ibsp.cache.client.structure.Del;
import ibsp.cache.client.structure.Exists;
import ibsp.cache.client.structure.Expire;
import ibsp.cache.client.structure.Get;
import ibsp.cache.client.structure.GetRange;
import ibsp.cache.client.structure.Getset;
import ibsp.cache.client.structure.HDel;
import ibsp.cache.client.structure.HExists;
import ibsp.cache.client.structure.HGet;
import ibsp.cache.client.structure.HGet4Bit;
import ibsp.cache.client.structure.HGetall;
import ibsp.cache.client.structure.HGetall4Bit;
import ibsp.cache.client.structure.HIncrby;
import ibsp.cache.client.structure.HKeys;
import ibsp.cache.client.structure.HLen;
import ibsp.cache.client.structure.HMSet;
import ibsp.cache.client.structure.HMget;
import ibsp.cache.client.structure.HMget4Bit;
import ibsp.cache.client.structure.HScan;
import ibsp.cache.client.structure.HSet;
import ibsp.cache.client.structure.HSetnx;
import ibsp.cache.client.structure.HVals;
import ibsp.cache.client.structure.Incr;
import ibsp.cache.client.structure.Incrby;
import ibsp.cache.client.structure.Lindex;
import ibsp.cache.client.structure.Linsert;
import ibsp.cache.client.structure.Llen;
import ibsp.cache.client.structure.Lpop;
import ibsp.cache.client.structure.Lpush;
import ibsp.cache.client.structure.Lpushx;
import ibsp.cache.client.structure.Lrange;
import ibsp.cache.client.structure.Lrem;
import ibsp.cache.client.structure.Lset;
import ibsp.cache.client.structure.Ltrim;
import ibsp.cache.client.structure.Persist;
import ibsp.cache.client.structure.Pexpire;
import ibsp.cache.client.structure.Pttl;
import ibsp.cache.client.structure.Rpop;
import ibsp.cache.client.structure.Rpush;
import ibsp.cache.client.structure.Rpushx;
import ibsp.cache.client.structure.SAdd;
import ibsp.cache.client.structure.SCard;
import ibsp.cache.client.structure.SIsmember;
import ibsp.cache.client.structure.SMembers;
import ibsp.cache.client.structure.SRem;
import ibsp.cache.client.structure.SetEx;
import ibsp.cache.client.structure.SetNx;
import ibsp.cache.client.structure.SetRange;
import ibsp.cache.client.structure.Strlen;
import ibsp.cache.client.structure.Ttl;
import ibsp.cache.client.structure.Type;
import ibsp.cache.client.structure.ZAdd;
import ibsp.cache.client.structure.ZCard;
import ibsp.cache.client.structure.ZCount;
import ibsp.cache.client.structure.ZIncrby;
import ibsp.cache.client.structure.ZRange;
import ibsp.cache.client.structure.ZRangeByScore;
import ibsp.cache.client.structure.ZRangeByScoreWithScores;
import ibsp.cache.client.structure.ZRangeWithScores;
import ibsp.cache.client.structure.ZRem;
import ibsp.cache.client.structure.ZRemrangeByRank;
import ibsp.cache.client.structure.ZRemrangeByScore;
import ibsp.cache.client.structure.ZScore;
import ibsp.cache.client.utils.CONSTS;
import ibsp.cache.client.utils.Tuple;

/**
 * 缓存层接口API实现类(单例 since v1.2.0) 
 * 
 */
public class CacheService extends BinaryCacheService implements ICacheService {
	
	private static CacheService instance = null;
	private static Object mtx = new Object();
	
	public static CacheService getInstance() {
		if (instance != null)
			return instance;
		synchronized(mtx) {
			if (instance==null) {
				Configuration config = Configuration.getInstance();
				instance = new CacheService(config.getServiceID(), config.getMetasvrUrl());
			}
		}
		return instance;
	}
	
	public static CacheService getInstance(String serviceID, String metasvrUrl) {
		if (instance != null)
			return instance;
		synchronized(mtx) {
			if (instance==null) {
				instance = new CacheService(serviceID, metasvrUrl);
			}
		}
		return instance;
	}

	private CacheService(String serviceID, String metasvrUrl) {
		super(serviceID, metasvrUrl);
	}
	
	public void close() {
		if (instance != null) {
			instance = null;
		}
		super.close();
	}

	@Override
	public boolean exists(final String key) {
		CacheRequest<Exists> request = new CacheRequest<Exists>();
		Exists param = new Exists();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Boolean)resp.getResult();
		}
		return false;
	}

//	@Override
//	public Long del(final String groupId, final String... keys) {
//		long result = FAILED_RESULT;
//		for(String key : keys) {
//			result = del(groupId, key);
//		}
//		return result;
//	}

	@Override
	public Long del(final String key) {
		CacheRequest<Del> request = new CacheRequest<Del>();		
		Del param = new Del();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String type(final String key) {
		CacheRequest<Type> request = new CacheRequest<Type>();
		Type param = new Type();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (String)resp.getResult();
		}
		return null;
	}

	@Override
	public Long expire(final String key, int seconds) {
		CacheRequest<Expire> request = new CacheRequest<Expire>();
		Expire param = new Expire();
		param.setKey(key);
		param.setSeconds(seconds);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			 return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String rename(String oldkey, String newkey) {
		CacheRequest<Get> request = new CacheRequest<Get>();
		Get getParam = new Get();
		getParam.setKey(oldkey);
		request.setParam(getParam);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode()) && resp.getResult() != null){
			del(oldkey);
			return set(newkey, (String)resp.getResult());
		}
		return CacheResponse.ERROR_CODE;
	}

	@Override
	public Long renamenx(String oldkey, String newkey) {
		CacheRequest<Get> request = new CacheRequest<Get>();
		Get getParam = new Get();
		getParam.setKey(oldkey);
		request.setParam(getParam);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode()) && resp.getResult() != null){
			del(oldkey);
			return setnx(newkey, resp.getResult().toString());
		}
		return FAILED_RESULT;
	}

	@Override
	public Long persist(String key) {
		CacheRequest<Persist> request = new CacheRequest<Persist>();
		Persist persist = new Persist();
		persist.setKey(key);
		request.setParam(persist);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			 return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long ttl(String key) {
		CacheRequest<Ttl> request = new CacheRequest<Ttl>();
		Ttl ttl = new Ttl();
		ttl.setKey(key);
		request.setParam(ttl);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		CacheRequest<Pexpire> request = new CacheRequest<Pexpire>();
		Pexpire pexpire = new Pexpire();
		pexpire.setKey(key);
		pexpire.setMilliseconds(milliseconds);
		request.setParam(pexpire);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			 return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long pttl(String key) {
		CacheRequest<Pttl> request = new CacheRequest<Pttl>();
		Pttl pttl = new Pttl();
		pttl.setKey(key);
		request.setParam(pttl);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String set(final String key, final String value) {
		CacheRequest<ibsp.cache.client.structure.Set> request = new CacheRequest<ibsp.cache.client.structure.Set>();
		ibsp.cache.client.structure.Set param = new ibsp.cache.client.structure.Set();
		param.setKey(key);
		param.setValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);
		return resp.getCode();
	}

	@Override
	public String set(final String key, final String value, int expireSeconds, boolean nx, boolean xx) {
		CacheRequest<ibsp.cache.client.structure.Set> request = new CacheRequest<ibsp.cache.client.structure.Set>();
		ibsp.cache.client.structure.Set param = new ibsp.cache.client.structure.Set();
		param.setKey(key);
		param.setValue(value);
		param.setSecond(expireSeconds);
		param.setNX(nx);
		param.setXX(xx);
		request.setParam(param);
		CacheResponse resp = execute(request);
		return resp.takeSetResult();
	}

	@Override
	public String get(final String key) {
		CacheRequest<Get> request = new CacheRequest<Get>();
		Get param = new Get();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode()) && resp.getResult() != null) {
			return ByteUtil.encode((byte[])resp.getResult());
		}
		return null;
	}

	@Override
	public Long strlen(String key) {
		CacheRequest<Strlen> request = new CacheRequest<Strlen>();
		Strlen strlen = new Strlen();
		strlen.setKey(key);
		request.setParam(strlen);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long append(String key, String value) {
		CacheRequest<Append> request = new CacheRequest<Append>();
		Append append = new Append();
		append.setKey(key);
		append.setValue(value);
		request.setParam(append);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long decrBy(String key, long decrement) {
		CacheRequest<Decrby> request = new CacheRequest<Decrby>();
		Decrby decrby = new Decrby();
		decrby.setKey(key);
		decrby.setInteger(decrement);
		request.setParam(decrby);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long decr(String key) {
		CacheRequest<Decr> request = new CacheRequest<Decr>();
		Decr decr = new Decr();
		decr.setKey(key);
		request.setParam(decr);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public byte[] getrange(String key, long startOffset, long endOffset) {
		CacheRequest<GetRange> request = new CacheRequest<GetRange>();
		GetRange getrange = new GetRange();
		getrange.setKey(key);
		getrange.setStartOffset(startOffset);
		getrange.setEndOffset(endOffset);
		request.setParam(getrange);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (byte[]) resp.getResult();
		}
		return null;
	}

	@Override
	public byte[] getSet(String key, String value) {
		CacheRequest<Getset> request = new CacheRequest<Getset>();
		Getset getset = new Getset();
		getset.setKey(key);
		getset.setValue(value);
		request.setParam(getset);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (byte[]) resp.getResult();
		}
		return null;
	}

	@Override
	public Long incr(String key) {
		CacheRequest<Incr> request = new CacheRequest<Incr>();
		Incr incr = new Incr();
		incr.setKey(key);
		request.setParam(incr);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long incrBy(String key, long increment) {
		CacheRequest<Incrby> request = new CacheRequest<Incrby>();
		Incrby incrby = new Incrby();
		incrby.setKey(key);
		incrby.setInteger(increment);
		request.setParam(incrby);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String setex(String key, int seconds, String value) {
		CacheRequest<SetEx> request = new CacheRequest<SetEx>();
		SetEx param = new SetEx();
		param.setKey(key);
		param.setSeconds(seconds);
		param.setValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);	
		return resp.getCode();
	}

	@Override
	public Long setnx(String key, String value) {
		CacheRequest<SetNx> request = new CacheRequest<SetNx>();
		SetNx param = new SetNx();
		param.setKey(key);
		param.setValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			 return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		CacheRequest<SetRange> request = new CacheRequest<SetRange>();
		SetRange setrange = new SetRange();
		setrange.setKey(key);
		setrange.setValue(ByteUtil.encode(value));
		setrange.setOffset(offset);
		request.setParam(setrange);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String lindex(String key, long index) {
		CacheRequest<Lindex> request = new CacheRequest<Lindex>();
		Lindex lindex = new Lindex();
		lindex.setKey(key);
		lindex.setIndex(index);
		request.setParam(lindex);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return new String((byte[])resp.getResult());
		}
		return null;
	}

	@Override
	public Long linsert(String key, boolean before, String pivot, String value) {
		CacheRequest<Linsert> request = new CacheRequest<Linsert>();
		Linsert linsert = new Linsert();
		linsert.setKey(key);
		linsert.setValue(ByteUtil.encode(value));
		linsert.setBefore(before);
		linsert.setPivot(ByteUtil.encode(pivot));
		request.setParam(linsert);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long llen(String key) {
		CacheRequest<Llen> request = new CacheRequest<Llen>();
		Llen llen = new Llen();
		llen.setKey(key);
		request.setParam(llen);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String lpop(String key) {
		CacheRequest<Lpop> request = new CacheRequest<Lpop>();
		Lpop lpop = new Lpop();
		lpop.setKey(key);
		request.setParam(lpop);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return new String((byte[])resp.getResult());
		}
		return null;
	}

	@Override
	public Long lpush(String key, String... values) {
		CacheRequest<Lpush> request = new CacheRequest<Lpush>();
		Lpush lpush = new Lpush();
		lpush.setKey(key);
		lpush.setValues(values);
		request.setParam(lpush);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long lpushx(String key, String... values) {
		CacheRequest<Lpushx> request = new CacheRequest<Lpushx>();
		Lpushx lpushx = new Lpushx();
		lpushx.setKey(key);
		lpushx.setValues(values);
		request.setParam(lpushx);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<byte[]> lrange(String key, long start, long end) {
		CacheRequest<Lrange> request = new CacheRequest<Lrange>();
		Lrange lrange = new Lrange();
		lrange.setKey(key);
		lrange.setStart(start);
		lrange.setEnd(end);
		request.setParam(lrange);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (List<byte[]>)resp.getResult();
		}
		return null;
	}

	@Override
	public Long lrem(String key, long count, String value) {
		CacheRequest<Lrem> request = new CacheRequest<Lrem>();
		Lrem lrem = new Lrem();
		lrem.setKey(key);
		lrem.setValue(ByteUtil.encode(value));
		lrem.setCount(count);
		request.setParam(lrem);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String lset(String key, long index, String value) {
		CacheRequest<Lset> request = new CacheRequest<Lset>();
		Lset lset = new Lset();
		lset.setKey(key);
		lset.setValue(ByteUtil.encode(value));
		lset.setIndex(index);
		request.setParam(lset);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return resp.getResult().toString();
		}
		return null;
	}

	@Override
	public String ltrim(String key, long start, long end) {
		CacheRequest<Ltrim> request = new CacheRequest<Ltrim>();
		Ltrim ltrim = new Ltrim();
		ltrim.setKey(key);
		ltrim.setStart(start);
		ltrim.setEnd(end);
		request.setParam(ltrim);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return resp.getResult().toString();
		}
		return null;
	}

	@Override
	public String rpop(String key) {
		CacheRequest<Rpop> request = new CacheRequest<Rpop>();
		Rpop rpop = new Rpop();
		rpop.setKey(key);
		request.setParam(rpop);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return new String((byte[])resp.getResult());
		}
		return null;
	}

	@Override
	public Long rpush(String key, String... values) {
		CacheRequest<Rpush> request = new CacheRequest<Rpush>();
		Rpush rpush = new Rpush();
		rpush.setKey(key);
		rpush.setValues(values);
		request.setParam(rpush);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long rpushx(String key, String value) {
		CacheRequest<Rpushx> request = new CacheRequest<Rpushx>();
		Rpushx rpushx = new Rpushx();
		rpushx.setKey(key);
		rpushx.setValues(value);
		request.setParam(rpushx);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long hdel(final String key, String... fields) {
		CacheRequest<HDel> request = new CacheRequest<HDel>();
		HDel param = new HDel();
		param.setKey(key);
		param.setFields(fields);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Boolean hexists(final String key, final String field) {
		CacheRequest<HExists> request = new CacheRequest<HExists>();
		HExists param = new HExists();
		param.setKey(key);
		param.setField(field);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Boolean)resp.getResult();
		}
		return false;
	}

	@Override
	public String hget(final String key, final String field) {
		CacheRequest<HGet> request = new CacheRequest<HGet>();
		HGet param = new HGet();
		param.setKey(key);
		param.setField(field);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (String)resp.getResult();
		}
		return null;
	}

	@Override
	public byte[] hget(final String key, final byte[] field) {
		CacheRequest<HGet4Bit> request = new CacheRequest<HGet4Bit>();
		HGet4Bit param = new HGet4Bit();
		param.setKey(key);
		param.setField(field);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (byte[])resp.getResult();
		}
		return null;	    
	}

	@SuppressWarnings("unchecked")	
	@Override
	public Set<String> hkeys(final String key) {
		CacheRequest<HKeys> request = new CacheRequest<HKeys>();
		HKeys param = new HKeys();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Set<String>)resp.getResult();
		}
		return null;
	}

	@Override
	public Long hset(final String key, final String field, final String value) {
		CacheRequest<HSet> request = new CacheRequest<HSet>();
		HSet param = new HSet();
		param.setKey(key);
		param.setField(field);
		param.setValue(value);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public String hmset(final String key, final Map<String, String> hash) {
		CacheRequest<HMSet> request = new CacheRequest<HMSet>();
		HMSet param = new HMSet();
		param.setKey(key);
		param.setHash(hash);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (String)resp.getResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> hgetAll(String key) {
		CacheRequest<HGetall> request = new CacheRequest<HGetall>();
		HGetall hgetall = new HGetall();
		hgetall.setKey(key);
		request.setParam(hgetall);
		CacheResponse resp = execute(request);		
		if (CacheResponse.OK_CODE.equals(resp.getCode()) ){
			return (Map<String, String>)resp.getResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		CacheRequest<HGetall4Bit> request = new CacheRequest<HGetall4Bit>();
		HGetall4Bit hgetall = new HGetall4Bit();
		hgetall.setKey(ByteUtil.encode(key));
		request.setParam(hgetall);
		CacheResponse resp = execute(request);      
		if (CacheResponse.OK_CODE.equals(resp.getCode()) ){
			return (Map<byte[], byte[]>)resp.getResult();
		}
		return null;
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		CacheRequest<HIncrby> request = new CacheRequest<HIncrby>();
		HIncrby hincrby = new HIncrby();
		hincrby.setKey(key);
		hincrby.setField(ByteUtil.encode(field));
		hincrby.setValue(value);
		request.setParam(hincrby);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long hlen(String key) {
		CacheRequest<HLen> request = new CacheRequest<HLen>();
		HLen hlen = new HLen();
		hlen.setKey(key);
		request.setParam(hlen);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@SuppressWarnings("unchecked")	
	@Override
	public List<byte[]> hmgetBytes(final String key, final String... fields) {
		CacheRequest<HMget4Bit> request = new CacheRequest<HMget4Bit>();
		HMget4Bit hmget = new HMget4Bit();
		hmget.setKey(key);
		hmget.setFields(fields);
		request.setParam(hmget);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode()) ){
			return (List<byte[]>)resp.getResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> hmget(final String key, final String... fields) {
		CacheRequest<HMget> request = new CacheRequest<HMget>();
		HMget hmget = new HMget();
		hmget.setKey(key);
		hmget.setFields(fields);
		request.setParam(hmget);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode()) ){
			return (List<String>)resp.getResult();
		}
		return null;	    
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		CacheRequest<HSetnx> request = new CacheRequest<HSetnx>();
		HSetnx hsetnx = new HSetnx();
		hsetnx.setKey(key);
		hsetnx.setValue(value);
		hsetnx.setField(field);
		request.setParam(hsetnx);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<byte[]> hvals(String key) {
		CacheRequest<HVals> request = new CacheRequest<HVals>();
		HVals hvals = new HVals();
		hvals.setKey(key);	
		request.setParam(hvals);
		CacheResponse resp = execute(request);
		if (CacheResponse.OK_CODE.equals(resp.getCode()) ){
			return (List<byte[]>)resp.getResult();
		}
		return null;
	}

	@Override
	public Long sadd(String key, String... members) {
		CacheRequest<SAdd> request = new CacheRequest<SAdd>();
		SAdd param = new SAdd();
		param.setKey(key);
		param.setMembers(members);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long scard(String key) {
		CacheRequest<SCard> request = new CacheRequest<SCard>();
		SCard param = new SCard();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public boolean sismember(String key, String member) {
		CacheRequest<SIsmember> request = new CacheRequest<SIsmember>();
		SIsmember param = new SIsmember();
		param.setKey(key);
		param.setMember(member);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Boolean)resp.getResult();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<byte[]> smembers(String key) {
		CacheRequest<SMembers> request = new CacheRequest<SMembers>();
		SMembers param = new SMembers();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Set<byte[]>)resp.getResult();
		}
		return null;
	}

	@Override
	public Long srem(String key, String... members) {
		CacheRequest<SRem> request = new CacheRequest<SRem>();
		SRem param = new SRem();
		param.setKey(key);
		param.setMembers(members);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long zadd(String key, double score, String member) {
		CacheRequest<ZAdd> request = new CacheRequest<ZAdd>();
		ZAdd param = new ZAdd();
		param.setKey(key);
		param.setMember(member);
		param.setScore(score);
		request.setParam(param);
		CacheResponse resp = execute(request);
        if(CacheResponse.OK_CODE.equals(resp.getCode())) {
            return (Long)resp.getResult();
        }
        return FAILED_RESULT;
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		CacheRequest<ZAdd> request = new CacheRequest<ZAdd>();
		ZAdd param = new ZAdd();
		param.setKey(key);
		param.setScoreMembers(scoreMembers);
		request.setParam(param);
		CacheResponse resp = execute(request);
        if(CacheResponse.OK_CODE.equals(resp.getCode())) {
            return (Long)resp.getResult();
        }
        return FAILED_RESULT;
	}

	@Override
	public Long zcard(String key) {
		CacheRequest<ZCard> request = new CacheRequest<ZCard>();
		ZCard param = new ZCard();
		param.setKey(key);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long zcount(String key, double min, double max) {
		CacheRequest<ZCount> request = new CacheRequest<ZCount>();
		ZCount param = new ZCount();
		param.setKey(key);
		param.setMax(max);
		param.setMin(min);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public double zincrby(String key, double score, String member) {
		CacheRequest<ZIncrby> request = new CacheRequest<ZIncrby>();
		ZIncrby param = new ZIncrby();
		param.setKey(key);
		param.setScore(score);
		param.setMember(member);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Double)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> zrange(String key, long start, long end) {
		CacheRequest<ZRange> request = new CacheRequest<ZRange>();
		ZRange param = new ZRange();
		param.setKey(key);
		param.setStart(start);
		param.setEnd(end);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			Set<Object> repSet=(Set<Object>)resp.getResult();
        	Set<String> retSet=new LinkedHashSet<String>() ;
        	for(Object obj:repSet){
        		if(obj instanceof byte[]){
        			retSet.add(new String((byte[])obj));
        		}else{
        			retSet.add((String)obj);
        		}
        	}
            return retSet;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		CacheRequest<ZRangeWithScores> request = new CacheRequest<ZRangeWithScores>();
		ZRangeWithScores param = new ZRangeWithScores();
		param.setKey(key);
		param.setStart(start);
		param.setEnd(end);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Set<Tuple>)resp.getResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		CacheRequest<ZRangeByScore> request = new CacheRequest<ZRangeByScore>();
		ZRangeByScore param = new ZRangeByScore();
		param.setKey(key);
		param.setMin(min);
		param.setMax(max);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			Set<Object> repSet=(Set<Object>)resp.getResult();
        	Set<String> retSet=new LinkedHashSet<String>() ;
        	for(Object obj:repSet){
        		if(obj instanceof byte[]){
        			retSet.add(new String((byte[])obj));
        		}else{
        			retSet.add((String)obj);
        		}
        	}
            return retSet;      
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		CacheRequest<ZRangeByScore> request = new CacheRequest<ZRangeByScore>();
		ZRangeByScore param = new ZRangeByScore();
		param.setKey(key);
		param.setMin(min);
		param.setMax(max);
		param.setOffset(offset);
		param.setCount(count);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			List<Object> repSet=(List<Object>)resp.getResult();
			Set<String> retSet=new LinkedHashSet<String>() ;
			for(Object obj : repSet){
				if(obj instanceof byte[]){
					retSet.add(new String((byte[])obj));
				}else{
					retSet.add((String)obj);
				}
			}
			return retSet;            
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		CacheRequest<ZRangeByScoreWithScores> request = new CacheRequest<ZRangeByScoreWithScores>();
		ZRangeByScoreWithScores param = new ZRangeByScoreWithScores();
		param.setKey(key);
		param.setMax(max);
		param.setMin(min);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Set<Tuple>)resp.getResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		CacheRequest<ZRangeByScoreWithScores> request = new CacheRequest<ZRangeByScoreWithScores>();
		ZRangeByScoreWithScores param = new ZRangeByScoreWithScores();
		param.setKey(key);
		param.setMax(max);
		param.setMin(min);
		param.setCount(count);
		param.setOffset(offset);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Set<Tuple>)resp.getResult();
		}
		return null;
	}

	@Override
	public Long zrem(String key, String... members) {
		CacheRequest<ZRem> request = new CacheRequest<ZRem>();
		ZRem param = new ZRem();
		param.setKey(key);
		param.setMembers(members);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public double zscore(String key, String member) {
		CacheRequest<ZScore> request = new CacheRequest<ZScore>();
		ZScore param = new ZScore();
		param.setKey(key);
		param.setMember(member);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Double)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		CacheRequest<ZRemrangeByRank> request = new CacheRequest<ZRemrangeByRank>();
		ZRemrangeByRank param = new ZRemrangeByRank();
		param.setKey(key);
		param.setStart(start);
		param.setEnd(end);
		request.setParam(param);
		CacheResponse resp = execute(request);
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public Long zremrangeByScore(String key, long start, long end) {
		CacheRequest<ZRemrangeByScore> request = new CacheRequest<ZRemrangeByScore>();
		ZRemrangeByScore param = new ZRemrangeByScore();
		param.setKey(key);
		param.setStart(start);
		param.setEnd(end);
		request.setParam(param);
		CacheResponse resp = execute(request);		
		if(CacheResponse.OK_CODE.equals(resp.getCode())) {
			return (Long)resp.getResult();
		}
		return FAILED_RESULT;
	}

	@Override
	public ScanResult<Map<byte[], byte[]>> hscan(String key, String cursor) {
		return hscan(key, cursor, new ScanParams());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ScanResult<Map<byte[], byte[]>> hscan(String key, String cursor, ScanParams params) {
		CacheRequest<HScan> request = new CacheRequest<HScan>();
		HScan hscan = new HScan();
		hscan.setKey(key);
		hscan.setCursor(ByteUtil.encode(cursor));
		hscan.setParams(params);
		request.setParam(hscan);
		CacheResponse resp = execute(request);		
		if (CacheResponse.OK_CODE.equals(resp.getCode())){
			return (ScanResult<Map<byte[], byte[]>>)resp.getResult();
		}
		return null;
	}

	@Override
	public boolean lock(String lockName, long expireTime) {
		String key = CONSTS.DEFAULT_LOCK_PRE + lockName;
		for(;;) {
			if (setnx(key, "locked") == 1) { // 获得锁
				if (expireTime > 0) { // 设置失效时间
					expire(key, (int)expireTime);
				}
				break; // 退出循环
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		return true;
	}


	@Override
	public boolean unlock(String lockName) {
		return del(CONSTS.DEFAULT_LOCK_PRE + lockName) > 0 ? true : false;
	}
}
