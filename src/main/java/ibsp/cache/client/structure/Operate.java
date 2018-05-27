package ibsp.cache.client.structure;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.cache.client.command.BinaryJedisCommands;
import ibsp.cache.client.utils.CONSTS.DataType;

public abstract class Operate<T, E extends BinaryJedisCommands> implements IExecutor<T, E> {
	public static final Long DEFAULT_ERROR = -1L;
    protected static final Logger log = LoggerFactory.getLogger(Operate.class);
	protected static final String SPLIT = "||";
	protected String key;
	protected String groupId;
	protected String command;
	protected long   respLength = 0;
	protected long   resqlength = 0;
	protected OperateType operateType;
	protected DataType dataType = DataType.STRING;
	protected boolean isByteValue = false;
	

	final public String getKey() {
		return key;
	}

	final public void setKey(String key) {
		this.key = key;
	}
	
	public final String getGroupId() {
		return groupId;
	}

	public final void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command){
		this.command = command;
	}
	
	public DataType getDataType() {
		return dataType;
	}
	
	public void setDataType(DataType dataType){
		this.dataType = dataType;
	}

	public OperateType getOperateType() {
		return operateType;
	}
	
	public void setOperateType(OperateType operateType){
		this.operateType = operateType;
	}
	
	public void setOperate(String command, OperateType operateType, DataType dataType){
		this.command = command;
		this.operateType = operateType;
		this.dataType = dataType;
	}

	public boolean isByteValue() {
		return isByteValue;
	}

	public void setByteValue(boolean isByteValue) {
		this.isByteValue = isByteValue;
	}
	
	public abstract byte[][] getParam();
	
	public abstract T doExecute(E jedis) throws Exception;
		
	public long getRespLength() {
		return this.respLength;
	}
	
	public long getResqlength() {
		return resqlength;
	}

	public void setResqlength(long resqlength) {
		this.resqlength = resqlength;
	}

	protected void setRespLength(long respLength) {
		this.respLength = respLength;
	}

	protected byte[][] joinParameters(byte[] first, String[] rest) {
		byte[][] result = new byte[rest.length + 1][];
		result[0] = first;
		for (int i = 0; i < rest.length; i++) {
			result[i + 1] = rest[i].getBytes();
		}
		return result;
	}
	
	//合并两个数组
	protected static <T> T[] concatArray(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Operate))
			return false;

		Operate operate = (Operate) o;

		if (command != null ? !command.equals(operate.command)	: operate.command != null)
			return false;
		if (operateType != operate.operateType)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = command != null ? command.hashCode() : 0;
		result = 31 * result + (operateType != null ? operateType.hashCode() : 0);
		return result;
	}
}
