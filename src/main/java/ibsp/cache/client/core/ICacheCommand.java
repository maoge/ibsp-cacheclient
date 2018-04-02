package ibsp.cache.client.core;

/**
 * 缓存层API接口。
 * key相关命令接口。
 * 
 * @author liujd
 *
 */
public interface ICacheCommand {
	/**
	 * 测试给定的key是否存在.
	 * 
	 * @param groupId
	 * @param key
	 * @return boolean 如果key存在则返回true，否则返回false
	 */
	public boolean exists(final String groupId, final String key);

	/**
	 * 方法无效,作废
	 * @param groupId
	 * @param keys
	 * @return Integer 返回大于等于0，如果key都不存在，则返回0。
	 */
	@Deprecated
	public Long del(final String groupId, final String... keys);
	
	/**
	 * 删除给定的key.
	 * 如果一个给定的key不存在，则对该key的删除动作无影响. 
	 * 该命令返回删除的key数量.
	 * 
	 * @param groupId
	 * @param key
	 * @return Integer 返回大于等于0，如果key都不存在，则返回0。
	 */
	public Long del(final String groupId, final String keys);

	/**
	 * 返回给定key所存储值的类型. 
	 * 类型为 "none", "string", "list", "set", "zset", "hash"中的一种. 
	 * 若给定key不存在，则返回"none".
	 * 
	 * @param groupId
	 * @param key
	 * @return String
	 */
	public String type(final String groupId, final String key);

	/**
	 * 重命名oldkey为newkey. 
	 * 若oldkey与newkey相同，则返回错误. 
	 * 若newkey已经存在，则将被覆盖.
	 * <p>
	 * 注：分布式环境下该操作不保证原子性.
	 * 
	 * @param groupId
	 * @param oldkey
	 * @param newkey
	 * @return String 如果成功返回OK，否则返回错误
	 * @see #renamenx(String, String, String)
	 */
	public String rename(final String groupId, final String oldkey, final String newkey);

	/**
	 * 重命名oldkey为newkey，若newkey已经存在，则操作失败.
	 * <p>
	 * 注：分布式环境下该操作不保证原子性.
	 * 
	 * @param groupId
	 * @param oldkey
	 * @param newkey
	 * @return Integer 成功返回1，若newkey已经存在则返回0
	 * @see #rename(String, String, String)
	 */
	public Long renamenx(final String groupId, final String oldkey, final String newkey);

	/**
	 * 设置给定key的生存时间，单位为秒. 
	 * 过了设定的生存时间后，key将被自动删除.
	 * 
	 * @param groupId
	 * @param key
	 * @param seconds
	 * @return Integer 设置成功返回1，当key不存在或不能为key设置超时时间则返回0.
	 * @see #persist(String, String)
	 */
	public Long expire(final String groupId, final String key, final int seconds);

	/**
	 * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』(一个不带生存时间、永不过期的 key ).
	 * 
	 * @param groupId
	 * @param key
	 * @return Integer 当生存时间移除成功时，返回 1；如果key不存在或 key没有设置生存时间，返回 0.
	 * @see #expire(String, String, int)
	 */
	public Long persist(final String groupId, final String key);

	/**
	 * 以秒为单位，返回给定 key的剩余生存时间.
	 * 
	 * @param groupId
	 * @param key
	 * @return Integer 当 key 不存在时，返回 -2.
	 *         当 key 存在但没有设置剩余生存时间时，返回 -1.
	 *         否则，以秒为单位，返回 key 的剩余生存时间.
	 * @see #expire(String, String, int)
	 */
	public Long ttl(final String groupId, final String key);

	/**
	 * 以毫秒为单位设置 key 的生存时间. 该命令作用和{@link #expire(String, String, int)}类似.
	 * 
	 * @param groupId
	 * @param key
	 * @param milliseconds
	 * @return Integer 设置成功返回1，key不存在或设置失败返回0.
	 */
	public Long pexpire(final String groupId, final String key, final long milliseconds);

	/**
	 * 以毫秒为单位，返回给定 key的剩余生存时间. 该命令作用和{@link #ttl(String, String)}类似.
	 * 
	 * @param groupId
	 * @param key
	 * @return Integer 当 key 不存在时，返回 -2.
	 *         当 key 存在但没有设置剩余生存时间时，返回 -1.
	 *         否则，以毫秒为单位，返回 key 的剩余生存时间.
	 */
	public Long pttl(final String groupId, final String key);
	
	/**
	 * 获取全局锁。 
	 * 若获得锁，且线程或进程异常退出，则超过失效时间后，锁自动释放。
	 * 
	 * @param groupId
	 * @param lockName
	 * @param expireTime 锁失效时间，单位为秒. 0为不失效.
	 * @return true 成功，false失败
	 */
	public boolean lock(final String groupId, final String lockName, final long expireTime);
	
	/**
	 * 释放全局锁。 
	 * 注：锁名字必须与调用{@link #lock(String, String, long)}时使用的名字相同。
	 * 
	 * @param groupId
	 * @param lockName
	 * @return
	 */
	public boolean unlock(final String groupId, final String lockName);
}
