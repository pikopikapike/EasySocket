package easysocket.serialize;

import easysocket.packet.ByteBufferManager;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public interface Serializable {


	void serialize(ByteBufferManager buffer);

	int getSerializingSize();

	static final Charset defaultCharset = StandardCharsets.UTF_8;

	public static int calculateSerializingSize(String str) {
		if (str == null || str.equals("")) {
			return 4;
		}
		int size = str.getBytes(defaultCharset).length;
		return size + 4;
	}

	public static <T extends Serializable> int calculateSerializingSize(Collection<T> list) {
		int size = 4;
		if (list != null) {
			for (T t : list) {
				size += (1 + t.getSerializingSize());
			}
		}
		return size;
	}

	public static <T extends Serializable> int calculateSerializingSize(T t) {
		return 1 + (t == null ? 0 : t.getSerializingSize());
	}

	public static int calculateSerializingSize(long[] array) {
		int size = 4;
		if (array != null) {
			size += 8 * array.length;
		}
		return size;
	}

	public static int calculateSerializingSize(int[] array) {
		int size = 4;
		if (array != null) {
			size += 4 * array.length;
		}
		return size;
	}

	public static int calculateSerializingSizeIntList(Collection<Integer> array) {
		int size = 4;
		if (array != null) {
			size += 4 * array.size();
		}
		return size;
	}

	public static int calculateSerializingSizeLongList(Collection<Long> array) {
		int size = 4;
		if (array != null) {
			size += 8 * array.size();
		}
		return size;
	}

	public static int calculateSerializingSize(float[] array) {
		int size = 4;
		if (array != null) {
			size += 4 * array.length;
		}
		return size;
	}

	public static int calculateSerializingSize(boolean[] array) {
		int size = 4;
		if (array != null) {
			size += array.length;
		}
		return size;
	}
}
