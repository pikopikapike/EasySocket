package easysocket.packet;

/**
 * 페킷 받는 빈도 체크 로직
 */
public class PacketFrequencyChecker {

	public static int PACKET_FREQUENCY_CHECK_DURATION = 2000; // ms
	public static int PACKET_FREQUENCY_CHECK_COUNT = 20;

	private long resetTime = 0;
	private int receiveCount;
	private final Runnable onError;

	public PacketFrequencyChecker(Runnable onError) {
		this.onError = onError;
	}

	/**
	 * 페킷수 증가 로직
	 * @param count
	 * @return 정상 처리 되였을 경우 true 반환; 비정상으로 페킷수 늘어 났을경우 false
	 */
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
