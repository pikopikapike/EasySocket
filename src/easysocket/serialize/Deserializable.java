package easysocket.serialize;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import easysocket.packet.ByteBufferManager;

public interface Deserializable {
	static Logger logger = LogManager.getLogger();
	public void deserialize(ByteBufferManager buffer);
}
