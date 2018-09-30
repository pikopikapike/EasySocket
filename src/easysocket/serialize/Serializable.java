package easysocket.serialize;

import easysocket.packet.ByteBufferManager;

public interface Serializable {
	void serialize(ByteBufferManager buffer);
}
