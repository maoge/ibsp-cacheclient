package ibsp.cache.client.core;

public class Respond {

	private long reqId;
	private byte[] resp;
	
	public long getReqId() {
		return reqId;
	}
	public void setReqId(long reqId) {
		this.reqId = reqId;
	}
	public byte[] getResp() {
		return resp;
	}
	public void setResp(byte[] resp) {
		this.resp = resp;
	}
	
	
}
