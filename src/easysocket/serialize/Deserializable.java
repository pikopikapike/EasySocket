package easysocket.serialize;

import easysocket.packet.ByteBufferManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Deserializable {
	static Logger logger = LogManager.getLogger();
	public void deserialize(ByteBufferManager buffer);
}
