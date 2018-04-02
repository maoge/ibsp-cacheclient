package ibsp.cache.client.protocol;

public class ScanResult<V> {
	private byte[]	cursor;
	private V results;

	public ScanResult() {	   
	}
	
	public ScanResult(String cursor, V results) {
		this( ByteUtil.encode( cursor ), results );
	}

	public ScanResult(byte[] cursor, V results) {
		this.cursor = cursor;
		this.results = results;
	}

	public String getStringCursor() {
		return ByteUtil.encode( cursor );
	}

	public byte[] getCursorAsBytes() {
		return cursor;
	}

	public V getResult() {
		return results;
	}

	public byte[] getCursor() {
		return cursor;
	}

	public void setCursor(byte[] cursor) {
		this.cursor = cursor;
	}

	public void setResults(V results) {
		this.results = results;
	}		
}
