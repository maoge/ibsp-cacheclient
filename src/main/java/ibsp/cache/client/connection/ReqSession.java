package ibsp.cache.client.connection;

import ibsp.cache.client.core.ParaEntity;

public class ReqSession {
	ParaEntity request;
	byte[] respond;
	long startReqTime;
	long endResptime;
	long processTimes;
	
	public ReqSession() {
	}

	public ParaEntity getRequest() {
		return request;
	}

	public void setRequest(ParaEntity request) {
		this.request = request;
	}

	public byte[] getRespond() {
		return respond;
	}

	public void setRespond(byte[] respond) {
		this.respond = respond;
	}

	public long getStartReqTime() {
		return startReqTime;
	}

	public void setStartReqTime(long startReqTime) {
		this.startReqTime = startReqTime;
	}

	public long getEndResptime() {
		return endResptime;
	}

	public void setEndResptime(long endResptime) {
		this.endResptime = endResptime;
	}

	public long getProcessTimes() {
		return processTimes;
	}

	public void setProcessTimes(long processTimes) {
		this.processTimes = processTimes;
	}
	
	
}
