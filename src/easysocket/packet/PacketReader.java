package easysocket.packet;

import easysocket.session.AioTcpSession;

public class PacketReader extends Packet {

	private AioTcpSession session;
	
	public PacketReader(byte[] data, AioTcpSession session) {
		super(data);
		this.setSession(session);
	}

	public AioTcpSession getSession() {
		return session;
	}

	public void setSession(AioTcpSession session) {
		this.session = session;
	}
}
