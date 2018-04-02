package ibsp.cache.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class Proxy {
	public static final Logger logger = LoggerFactory.getLogger(Proxy.class);
	
	private String ID;
	private String address;
	private String name;
	private String groupID;

	public Proxy(JSONObject o, String groupID) {
		this.ID = o.getString("ID");
		this.address = o.getString("IP")+":"+o.getString("PORT");
		this.name = o.getString("NAME");
		this.groupID = groupID;
	}

	public String getID() {
		return ID;
	}
	
	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}
	
	public String getGroupID() {
		return groupID;
	}
	
	@Override
	public String toString() {
		return this.name+" "+this.address;
	}
}