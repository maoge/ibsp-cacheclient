package ibsp.cache.client.command;

public interface BasicCommands {

	String ping();

	String quit();

	Long dbSize();

	String select(int index);

	String auth(String password);

	String save();

	String bgsave();

	String bgrewriteaof();

	Long lastsave();

	String shutdown();

	String info();

	String info(String section);

	String slaveof(String host, int port);

	String slaveofNoOne();

	String configResetStat();

	Long waitReplicas(int replicas, long timeout);

}
