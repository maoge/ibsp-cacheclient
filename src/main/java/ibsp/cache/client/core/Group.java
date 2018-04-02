package ibsp.cache.client.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Group {
    public static final Logger logger = LoggerFactory.getLogger(Group.class);
    
    private String             groupId;
    
    private boolean            direct = false;
    
    private List<String>       hosts  = new ArrayList<String>();
    
    private Random             ran = new Random();
    
    private String             groupConfig;
    
    public Group() {
    	this.groupId = null;
    }
    
    public Group(String groupId) {
        this.groupId = groupId;
    }
    
    public Group(String groupId, String groupConfig) {
    	this.groupId = groupId;
    	this.groupConfig = groupConfig;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getGroupId());
        buf.append("\n{ ");
        buf.append("hosts:\n");
        for (String h : hosts) {
            buf.append(h + "\n");
        }
        buf.append("}\n");
        return buf.toString();
    }
        
    public List<String> getHosts() {
        return hosts;
    }
    
    public boolean isDirect() {
		return direct;
	}

	public void setDirect(boolean direct) {
		this.direct = direct;
	}

    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public void addHost(String host) {
        this.hosts.add(host);
    }
    
    public void clearHost() {
    	this.hosts.clear();
    }
        
    public String getGroupConfig() {
		return groupConfig;
	}

	/**
     * 
     * 从Group配置的接入机中随机选择一个可用接入机,排除有异常的接入机 
     * @param excludeProxy
     * @return
     */
    public String getRandomHost(Collection<String> excludeProxy) {
        int k = hosts.size();
        String proxyName = null;
        Map<String, Integer> checkedMap = new HashMap<String, Integer>();
        do {
             int index = ran.nextInt(hosts.size());
             proxyName = hosts.get(index);
             if(excludeProxy.contains(proxyName)) {
                 if(!checkedMap.containsKey(proxyName)) {
                     checkedMap.put(proxyName, 1);
                     k--;         
                 }
                 proxyName = null;
             } else {
                 break;
             }
        } while(k > 0);
        checkedMap.clear();
        return proxyName;
    }
    
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Group) {
            Group g = (Group) obj;
            if (g.getGroupId().toUpperCase().equals(this.getGroupId().toUpperCase())) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }
        
    public void setHosts(List<String> hosts) {
    	this.hosts.clear();
    	this.hosts.addAll(hosts);
    }
}
