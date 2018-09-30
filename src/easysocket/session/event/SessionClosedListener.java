package easysocket.session.event;

import java.util.List;

import easysocket.packet.PacketReader;

@FunctionalInterface
public interface SessionClosedListener extends SessionEventListener {
	@Override
	public default void onReceivePackets(List<PacketReader> packets) {
		// do nothing
	}
}
