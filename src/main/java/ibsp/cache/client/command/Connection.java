package ibsp.cache.client.command;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import ibsp.cache.client.exception.RedisConnectionException;
import ibsp.cache.client.exception.RedisDataException;
import ibsp.cache.client.protocol.BuilderFactory;
import ibsp.cache.client.protocol.IOUtils;
import ibsp.cache.client.protocol.Protocol;
import ibsp.cache.client.protocol.ProtocolCommand;
import ibsp.cache.client.protocol.RedisInputStream;
import ibsp.cache.client.protocol.RedisOutputStream;
import ibsp.cache.client.protocol.SafeEncoder;

public class Connection implements Closeable {

  private String host = Protocol.DEFAULT_HOST;
  private int port = Protocol.DEFAULT_PORT;
  private Socket socket;
  private RedisOutputStream outputStream;
  private RedisInputStream inputStream;
  private int pipelinedCommands = 0;
  private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
  private int soTimeout = Protocol.DEFAULT_TIMEOUT;
  private boolean broken = false;

  public Connection() {
  }

  public Connection(final String host) {
    this.host = host;
  }

  public Connection(final String host, final int port) {
    this.host = host;
    this.port = port;
  }

  public Socket getSocket() {
    return socket;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public int getSoTimeout() {
    return soTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public void setSoTimeout(int soTimeout) {
    this.soTimeout = soTimeout;
  }

  public void setTimeoutInfinite() {
    try {
      if (!isConnected()) {
        connect();
      }
      socket.setSoTimeout(0);
    } catch (SocketException ex) {
      broken = true;
      throw new RedisConnectionException(ex);
    }
  }

  public void rollbackTimeout() {
    try {
      socket.setSoTimeout(soTimeout);
    } catch (SocketException ex) {
      broken = true;
      throw new RedisConnectionException(ex);
    }
  }

  protected Connection sendCommand(final ProtocolCommand cmd, final String... args) {
    final byte[][] bargs = new byte[args.length][];
    for (int i = 0; i < args.length; i++) {
      bargs[i] = SafeEncoder.encode(args[i]);
    }
    return sendCommand(cmd, bargs);
  }

  protected Connection sendCommand(final ProtocolCommand cmd, final byte[]... args) {
    try {
      connect();
      Protocol.sendCommand(outputStream, cmd, args);
      pipelinedCommands++;
      return this;
    } catch (RedisConnectionException ex) {
      // Any other exceptions related to connection?
      broken = true;
      throw ex;
    }
  }

  protected Connection sendCommand(final ProtocolCommand cmd) {
    try {
      connect();
      Protocol.sendCommand(outputStream, cmd, new byte[0][]);
      pipelinedCommands++;
      return this;
    } catch (RedisConnectionException ex) {
      // Any other exceptions related to connection?
      broken = true;
      throw ex;
    }
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public void connect() {
    if (!isConnected()) {
      try {
        socket = new Socket();
        // ->@wjw_add
        socket.setReuseAddress(true);
        socket.setKeepAlive(true); // Will monitor the TCP connection is
        // valid
        socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
        // ensure timely delivery of data
        socket.setSoLinger(true, 0); // Control calls close () method,
        // the underlying socket is closed
        // immediately
        // <-@wjw_add

        socket.connect(new InetSocketAddress(host, port), connectionTimeout);
        socket.setSoTimeout(soTimeout);
        outputStream = new RedisOutputStream(socket.getOutputStream());
        inputStream = new RedisInputStream(socket.getInputStream());
      } catch (IOException ex) {
        broken = true;
        throw new RedisConnectionException(ex);
      }
    }
  }

  @Override
  public void close() {
    disconnect();
  }

  public void disconnect() {
    if (isConnected()) {
      try {
        outputStream.flush();
        socket.close();
      } catch (IOException ex) {
        broken = true;
        throw new RedisConnectionException(ex);
      } finally {
        IOUtils.closeQuietly(socket);
      }
    }
  }

  public boolean isConnected() {
    return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()
        && !socket.isInputShutdown() && !socket.isOutputShutdown();
  }

  public String getStatusCodeReply() {
    flush();
    pipelinedCommands--;
    final byte[] resp = (byte[]) readProtocolWithCheckingBroken();
    if (null == resp) {
      return null;
    } else {
      return SafeEncoder.encode(resp);
    }
  }

  public String getBulkReply() {
    final byte[] result = getBinaryBulkReply();
    if (null != result) {
      return SafeEncoder.encode(result);
    } else {
      return null;
    }
  }

  public byte[] getBinaryBulkReply() {
    flush();
    pipelinedCommands--;
    return (byte[]) readProtocolWithCheckingBroken();
  }

  public Long getIntegerReply() {
    flush();
    pipelinedCommands--;
    return (Long) readProtocolWithCheckingBroken();
  }

  public List<String> getMultiBulkReply() {
    return BuilderFactory.STRING_LIST.build(getBinaryMultiBulkReply());
  }

  @SuppressWarnings("unchecked")
  public List<byte[]> getBinaryMultiBulkReply() {
    flush();
    pipelinedCommands--;
    return (List<byte[]>) readProtocolWithCheckingBroken();
  }

  public void resetPipelinedCount() {
    pipelinedCommands = 0;
  }

  @SuppressWarnings("unchecked")
  public List<Object> getRawObjectMultiBulkReply() {
    return (List<Object>) readProtocolWithCheckingBroken();
  }

  public List<Object> getObjectMultiBulkReply() {
    flush();
    pipelinedCommands--;
    return getRawObjectMultiBulkReply();
  }

  @SuppressWarnings("unchecked")
  public List<Long> getIntegerMultiBulkReply() {
    flush();
    pipelinedCommands--;
    return (List<Long>) readProtocolWithCheckingBroken();
  }

  public List<Object> getAll() {
    return getAll(0);
  }

  public List<Object> getAll(int except) {
    List<Object> all = new ArrayList<Object>();
    flush();
    while (pipelinedCommands > except) {
      try {
        all.add(readProtocolWithCheckingBroken());
      } catch (RedisDataException e) {
        all.add(e);
      }
      pipelinedCommands--;
    }
    return all;
  }

  public Object getOne() {
    flush();
    pipelinedCommands--;
    return readProtocolWithCheckingBroken();
  }

  public boolean isBroken() {
    return broken;
  }

  protected void flush() {
    try {
      outputStream.flush();
    } catch (IOException ex) {
      broken = true;
      throw new RedisConnectionException(ex);
    }
  }

  protected Object readProtocolWithCheckingBroken() {
    try {
      return Protocol.read(inputStream);
    } catch (RedisConnectionException exc) {
      broken = true;
      throw exc;
    }
  }
}
