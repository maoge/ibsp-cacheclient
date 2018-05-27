package ibsp.cache.client.core;

import ibsp.cache.client.protocol.Protocol.Command;

public class ParaEntity {
	private Command command;
    private byte[][] args;
    private final Nheader header ;
    
	public Nheader getHeader() {
		return header;
	}

	public ParaEntity(Command command, Nheader header) {
		this(command, header,null);
	}
	
	public ParaEntity(Command command, Nheader header, byte[][] args) {
		this.header = header;
		this.command = command;
		this.args = args;
	}

	public Command getCommand() {
		return command;
	}
	
	public byte[][] getArgs() {
		return args;
	}

	public Object getReqId() {
		return header.getReqid();
	}
	
}
