package easysocket.session;

public class SessionState {

	public static final int UNKNOWN = 0;
	public static final int OPENED = 1;
	public static final int CLOSED = 2;
	public static final int PACKETPROCESSING = 3;
	public static final int PACKETBLOCKED = 4;

	private int state0;

	public SessionState(int state) {
		this.state0 = state;
	}

	public void set(int state) {
		this.state0 = state;
	}

	public int getState() {
		return this.state0;
	}
}