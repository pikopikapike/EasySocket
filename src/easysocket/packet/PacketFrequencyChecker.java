package easysocket.packet;

public class PacketFrequencyChecker {

	public static int PACKET_FREQUENCY_CHECK_DURATION = 2000; // ms
	public static int PACKET_FREQUENCY_CHECK_COUNT = 20;

	private long resetTime = 0;
	private int receiveCount;
	private final Runnable onError;

	public PacketFrequencyChecker(Runnable onError) {
		this.onError = onError;
	}

	public boolean increaseReceiveCount(int count) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - resetTime > PACKET_FREQUENCY_CHECK_DURATION) {
			resetTime = currentTime;
			receiveCount = 0;
		}
		receiveCount += count;

		if (receiveCount >= PACKET_FREQUENCY_CHECK_COUNT) {
			onError.run();
			return false;
		} else {
			return true;
		}
	}
}
