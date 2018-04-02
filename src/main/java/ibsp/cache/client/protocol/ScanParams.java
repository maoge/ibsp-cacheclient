package ibsp.cache.client.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ScanParams {
  private List<byte[]> params = new ArrayList<byte[]>();
  public final static String SCAN_POINTER_START = String.valueOf(0);
  public final static byte[] SCAN_POINTER_START_BINARY = ByteUtil.encode(SCAN_POINTER_START);

  public ScanParams match(final byte[] pattern) {
    params.add(Keyword.MATCH.raw);
    params.add(pattern);
    return this;
  }

  public ScanParams match(final String pattern) {
    params.add(Keyword.MATCH.raw);
    params.add(ByteUtil.encode(pattern));
    return this;
  }

  public ScanParams count(final int count) {
    params.add(Keyword.COUNT.raw);
    params.add(ByteUtil.encode(String.valueOf(count)));
    return this;
  }

  public Collection<byte[]> getParams() {
    return Collections.unmodifiableCollection(params);
  }
}
