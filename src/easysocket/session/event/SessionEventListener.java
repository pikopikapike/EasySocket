package easysocket.session.event;

import java.util.List;

import easysocket.packet.PacketReader;

public interface SessionEventListener {
	void onClose();
	void onReceivePackets(List<PacketReader> packets);
}