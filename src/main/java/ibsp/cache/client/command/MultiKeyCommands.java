package ibsp.cache.client.command;

import java.util.List;
import java.util.Set;

import ibsp.cache.client.protocol.BitOP;
import ibsp.cache.client.protocol.SortingParams;
import ibsp.cache.client.protocol.ZParams;

public interface MultiKeyCommands {

	Long del(String... keys);

	List<String> blpop(int timeout, String... keys);

	List<String> brpop(int timeout, String... keys);

	List<String> blpop(String... args);

	List<String> brpop(String... args);

	Set<String> keys(String pattern);

	List<String> mget(String... keys);

	String mset(String... keysvalues);

	Long msetnx(String... keysvalues);

	String rename(String oldkey, String newkey);

	Long renamenx(String oldkey, String newkey);

	String rpoplpush(String srckey, String dstkey);

	Set<String> sdiff(String... keys);

	Long sdiffstore(String dstkey, String... keys);

	Set<String> sinter(String... keys);

	Long sinterstore(String dstkey, String... keys);

	Long smove(String srckey, String dstkey, String member);

	Long sort(String key, SortingParams sortingParameters, String dstkey);

	Long sort(String key, String dstkey);

	Set<String> sunion(String... keys);

	Long sunionstore(String dstkey, String... keys);

	String watch(String... keys);

	String unwatch();

	Long zinterstore(String dstkey, String... sets);

	Long zinterstore(String dstkey, ZParams params, String... sets);

	Long zunionstore(String dstkey, String... sets);

	Long zunionstore(String dstkey, ZParams params, String... sets);

	String brpoplpush(String source, String destination, int timeout);

	String randomKey();

	Long bitop(BitOP op, final String destKey, String... srcKeys);

}
