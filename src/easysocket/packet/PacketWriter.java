package easysocket.packet;

public class PacketWriter extends Packet {

	static final int defaultPacketSize = 128;

	protected PacketWriter(int cmd, int initialBufferSize) {
		super(cmd, initialBufferSize);
	}

	public static PacketWriter init(int cmd) {
		PacketWriter packet = new PacketWriter(cmd, defaultPacketSize);
		return packet;
	}

	public static PacketWriter init(int cmd, int errorCode) {
		PacketWriter packet = new PacketWriter(cmd, defaultPacketSize);
		packet.putInt(errorCode);
		return packet;
	}
	
	public static PacketWriter init(int cmd, int errorCode, int initialBufferSize) {
		PacketWriter packet = new PacketWriter(cmd, initialBufferSize);
		packet.putInt(errorCode);
		return packet;
	}
}
