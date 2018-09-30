package easysocket.packet;

public abstract class Packet extends ByteBufferManager {
	private final int cmd;

	protected Packet(byte[] data) {
		super(data);
		this.cmd = getInt();
	}

	protected Packet(int cmd, int initialBufferSize) {
		super(initialBufferSize);
		this.cmd = cmd;
		putInt(this.cmd);
	}

	public int getCmd() {
		return cmd;
	}
}
