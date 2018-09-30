package easysocket.packet;

import easysocket.session.AioTcpSession;

public class SrlPacketReader extends PacketReader {

	private final long srl;

	public SrlPacketReader(byte[] data, AioTcpSession session) {
		super(data, session);
		this.srl = getLong();
	}

	public SrlPacketReader(PacketReader packet) {
		this(packet.getData(), packet.getSession());
	}

	public long getSrl() {
		return srl;
	}
}
