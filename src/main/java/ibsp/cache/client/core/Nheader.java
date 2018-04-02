package ibsp.cache.client.core;

import java.util.concurrent.atomic.AtomicLong;

public class Nheader {
	private String groupId;
	private long reqid;
	public static AtomicLong ids = new AtomicLong();
	public static final long MAX_IDS = 1000000000000000000l;
//	public static final SimpleDateFormat df = new SimpleDateFormat("hhmmss");


	public Nheader(String groupId) {
	    this.groupId = groupId;
	    long id = ids.incrementAndGet();
	    if (id > MAX_IDS) {
	    	ids.set(0);
	    }
	    reqid = id ;//Long.parseLong(df.format(new Date())+id%MAX_IDS);
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public long getReqid() {
		return reqid;
	}

	public void setReqid(long reqid) {
		this.reqid = reqid;
	}
	
	
}
