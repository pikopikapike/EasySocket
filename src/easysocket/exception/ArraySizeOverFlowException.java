package easysocket.exception;

public class ArraySizeOverFlowException extends RuntimeException {

	private static final long serialVersionUID = -1999069468685177468L;

	private final int length;

	public ArraySizeOverFlowException(int length) {
		this.length = length;
	}

	public int length() {
		return this.length;
	}
}
