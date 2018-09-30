package easysocket.packet;

public class SrlPacketWriter extends PacketWriter {

	protected SrlPacketWriter(long srl, int cmd, int initialBufferSize) {
		super(cmd, initialBufferSize);
		this.putLong(srl);
	}

	public static SrlPacketWriter init(long srl, int cmd) {
		SrlPacketWriter packet = new SrlPacketWriter(srl, cmd, defaultPacketSize);
		return packet;
	}

	public static SrlPacketWriter init(long srl, int cmd, int errorCode) {
		SrlPacketWriter packet = new SrlPacketWriter(srl, cmd, defaultPacketSize);
		packet.putInt(errorCode);
		return packet;
	}

	public static SrlPacketWriter init(long srl, int cmd, int errorCode, int byteSize) {
		SrlPacketWriter packet = new SrlPacketWriter(srl, cmd, byteSize + 4 + 8 + 4);
		packet.putInt(errorCode);
		return packet;
	}
}
