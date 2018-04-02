package ibsp.cache.client.core;

public interface IBinaryCacheService {
	/**
	 * 是否设置key的默认失效时间.
	 * 
	 * @param isDoKeyExpire true设置key的默认失效时间, false不设置
	 */
//	public void setDoKeyExpire(boolean isDoKeyExpire);
	
	// ------------------------------- string --------------------------- //
	/**
	 * 设置给定key的字符串值.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return 成功返回OK，未设置返回null
	 */
	public String set(final String groupId, final String key, byte[] value);

	/**
	 * 返回给定key的值. 如果key不存在，则返回null. 如果key的类型不是string，则返回错误.
	 * 
	 * @param groupId
	 * @param key
	 * @return 返回key对应的值. 如果key不存在，则返回null. 如果key的类型不是string，则返回错误
	 * @see ICacheService#get(String, String)
	 */
	public byte[] getBytes(final String groupId, final String key);

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return 返回给定 key 的旧值，当key不存在时返回null
	 */
	public byte[] getSet(final String groupId, final String key, final byte[] value);

	/**
	 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位). SETEX
	 * 是一个原子性(atomic)操作，关联值和设置生存时间两个动作会在同一时间内完成.
	 * 
	 * @param groupId
	 * @param key
	 * @param seconds
	 * @param value
	 * @return 设置成功时返回 OK，当 seconds 参数不合法时返回一个错误.
	 */
	public String setex(final String groupId, final String key, final int seconds, final byte[] value);

	/**
	 * 将 key 的值设为 value ，当且仅当 key 不存在. 若给定的 key 已经存在，则 SETNX 不做任何动作.
	 * 
	 * @param groupId
	 * @param key
	 * @param value
	 * @return Integer 设置成功返回1，失败返回0.
	 */
	public Long setnx(final String groupId, final String key, final byte[] value);
	
//	public void setAllowLog(final boolean bAllowLog);
}
