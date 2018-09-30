package easysocket.session.event;

@FunctionalInterface
public interface SessionReceivedPacketListener extends SessionEventListener {
	@Override
	public default void onClose() {
		// do nothing
	}
}
