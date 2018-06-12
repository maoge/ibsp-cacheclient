package ibsp.cache.client.connection;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.core.ParaEntity;
import ibsp.cache.client.core.ProxyRedisOutputStream;
import ibsp.cache.client.core.Respond;
import ibsp.cache.client.exception.ProxyRedisException;
import ibsp.cache.client.exception.ProxyRedisException.PROXYERROR;
import ibsp.cache.client.protocol.Protocol;
import ibsp.cache.client.protocol.RedisOutputStream;
import ibsp.cache.client.protocol.SafeEncoder;
import ibsp.common.utils.CONSTS;

public abstract class ParaConversion {
	public static final Logger logger = LoggerFactory.getLogger(ParaConversion.class);
	private static final long[]  sizeTable = { 9L,
			99L,
			999L,
			9999L,
			99999L,
			999999L,
			9999999L,
			99999999L,
			999999999L,
			9999999999L,
			99999999999L,
			999999999999L,
			9999999999999L,
			99999999999999L,
			999999999999999L,
			9999999999999999L,
			99999999999999999L,
			999999999999999999L,
			Long.MAX_VALUE };
	private static final byte[] SPLIT_BYTES = { 0x15, 0x15 };
	private static final ThreadLocal<ByteBuffer> ThreadBuf = new ThreadLocal<ByteBuffer>();
	private ProxyRedisOutputStream proxyout = new ProxyRedisOutputStream();
	
	private int getValueLength(long value) {
		int length = 0;
		while (value > sizeTable[length])
			length++;
		length++;
		return length;
	}
	
	private void writeLong(RedisOutputStream out, long value) throws IOException {
		if (value < 10) {
			out.write((byte) ('0' + value));
		} else {
			out.write(("" + value).getBytes());
		}
	}
	
	private RedisOutputStream get(OutputStream out){
		return proxyout.getSameRedisOutputStream(out);
	}

	public void write(ParaEntity para, OutputStream out) throws IOException {
		
		byte[][] args = para.getArgs();
		byte[] command = SafeEncoder.encode(para.getCommand().name());
		int respLen = 1 + getValueLength(args==null?1:args.length + 1) + 2 + 1 + getValueLength(command.length) + 2 + command.length + 2;
		for (final byte[] arg : args)
			respLen += (1 + getValueLength(arg.length) + 2 + arg.length + 2);
		
		long reqId = (Long)para.getReqId();
		byte[] groupId =SafeEncoder.encode(para.getHeader().getGroupId());
		int headLen = command.length + 2 + (args==null||args.length==0?0:args[0].length) + 2 + groupId.length + 2 + getValueLength(reqId) + 2 + getValueLength(respLen);

		if(logger.isDebugEnabled()) {
			logger.debug("sendReqId:"+para.getReqId());			
		}
		
		RedisOutputStream jedisout = get(out);
		jedisout.write(Protocol.DOLLAR_BYTE);
		jedisout.writeIntCrLf(headLen);
		jedisout.write(command);
		jedisout.write(SPLIT_BYTES);
		jedisout.write((args==null||args.length==0)?SafeEncoder.encode(""):args[0]);
		jedisout.write(SPLIT_BYTES);
		jedisout.write(groupId);
		jedisout.write(SPLIT_BYTES);
		writeLong(jedisout, reqId);
		jedisout.write(SPLIT_BYTES);
		jedisout.writeIntCrLf(respLen);
		
		if (para.getArgs() == null || para.getArgs().length == 0) {
			Protocol.sendCommand(jedisout, para.getCommand());
		}else if (para.getArgs().length == 1) {
			Protocol.sendCommand(jedisout, para.getCommand(),para.getArgs()[0]);
		}else {
			Protocol.sendCommand(jedisout, para.getCommand(),para.getArgs());
		}
		jedisout.flush();
	}

	protected Respond readRespond(final InputStream is) throws ProxyRedisException, IOException {
		
		int r = is.read();
		if( r == -1) {
			throw new IOException();
		}
		byte b = (byte) r;
		if (b != Protocol.DOLLAR_BYTE) {
			return null;
		}
		long reqId = readLong(is);
		if (is.read() != '\r' || is.read() != '\n' ){
			throw new ProxyRedisException("expect \r\n").setErrorCode(PROXYERROR.e8);
		}
		b = (byte) is.read();
		
		if (b != Protocol.DOLLAR_BYTE) {
			throw new ProxyRedisException("expect "+Character.valueOf((char) Protocol.DOLLAR_BYTE)).setErrorCode(PROXYERROR.e8);
		}
		
		int respLen = readInt(is);
		
		if(logger.isDebugEnabled()) {
			logger.debug("respReqId:"+reqId + ",respLength"+respLen);			
		}

		if (is.read() != '\r' || is.read() != '\n' ){
			throw new ProxyRedisException("expect \r\n").setErrorCode(PROXYERROR.e8);
		}

		byte[] resp = new byte[respLen];
		if (respLen > CONSTS.MAX_REDIS_BODY) {
			throw new ProxyRedisException("返回报文超长 \r\n").setErrorCode(PROXYERROR.e10);
		}
		
		int read =  is.read(resp);
		while (read < respLen ) {
			read +=  is.read(resp,read,respLen - read);
		}

		Respond respond = new Respond();
		respond.setReqId(reqId);
		respond.setResp(resp);
		return respond;
		
	}
	
    public final long readLong(InputStream in) throws IOException {
    	byte[] readBuffer = new byte[8];
        int n = 0;
        while (n < readBuffer.length) {
            int count = in.read(readBuffer, 0 + n, readBuffer.length - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
		ByteBuffer bf = getLengthBuff();
		bf.clear();
		bf.put(readBuffer);
		bf.flip();
		return bf.getLong();
    }

    public final int readInt(InputStream in) throws IOException {
    	byte[] ch = new byte[4];
        int n = 0;
        while (n < ch.length) {
            int count = in.read(ch, 0 + n, ch.length - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
		ByteBuffer bf = getLengthBuff();
		bf.clear();
		bf.put(ch);
		bf.flip();
		return bf.getInt();
    }
    
	protected ByteBuffer getLengthBuff() {
		ByteBuffer bf = ThreadBuf.get();
		if (bf == null) {
			bf = ByteBuffer.allocate(8);
			ThreadBuf.set(bf);
		}
		return bf;
	}
}
