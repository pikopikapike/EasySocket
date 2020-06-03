package easysocket.packet;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import easysocket.exception.ArraySizeOverFlowException;
import easysocket.serialize.Deserializable;
import easysocket.serialize.Serializable;
import easysocket.session.AioTcpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

public class ByteBufferManager {
	static Logger logger = LogManager.getLogger(ByteBufferManager.class);

	static final Charset defaultCharset = Charset.forName("UTF-8");

	static final int MAX_ARRAY_SIZE = 99999;

	private ByteBuffer buffer;

	public ByteBufferManager(int initialBufferSize) {
		buffer = ByteBuffer.allocate(initialBufferSize);
		buffer.order(AioTcpSession.BYTE_ORDER);
	}

	public ByteBufferManager(byte[] data) {
		buffer = ByteBuffer.wrap(data);
		buffer.order(AioTcpSession.BYTE_ORDER);
	}

	public ByteBufferManager(ByteBuffer buffer) {
		this.buffer = buffer;
		buffer.order(AioTcpSession.BYTE_ORDER);
		buffer.limit(buffer.position());
		buffer.position(0);
	}

	public void flip() {
		this.buffer.flip();
	}

	public int limit() {
		return this.buffer.limit();
	}

	public byte[] array() {
		return this.buffer.array();
	}

	private void checkArrayList(int size) {
		if (size > MAX_ARRAY_SIZE) {
			throw new ArraySizeOverFlowException(size);
		}
	}

	public <T extends Serializable> void putObject(T t) {
		boolean isNull = t == null;
		this.putBoolean(isNull);

		if (!isNull)
			t.serialize(this);
	}

	public <T extends Serializable> void putObjectList(Collection<T> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (T t : list) {
			this.putObject(t);
		}
	}

	public <T extends Serializable> void putObjectArray(T[] array) {
		if (array == null || array.length == 0) {
			this.putInt(0);
			return;
		}
		int size = array.length;
		checkArrayList(size);
		this.putInt(size);
		for (T t : array) {
			this.putObject(t);
		}
	}

	public void putIntList(Collection<Integer> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (int i : list) {
			this.putInt(i);
		}
	}

	public void putIntArray(int[] array) {
		if (array == null || array.length == 0) {
			this.putInt(0);
			return;
		}
		int size = array.length;
		checkArrayList(size);
		this.putInt(size);
		for (int i : array) {
			this.putInt(i);
		}
	}

	public void putBooleanArray(boolean[] array) {
		if (array == null || array.length == 0) {
			this.putInt(0);
			return;
		}
		int size = array.length;
		checkArrayList(size);
		this.putInt(size);
		for (boolean i : array) {
			this.putBoolean(i);
		}
	}

	public void putLongArray(long[] array) {
		if (array == null || array.length == 0) {
			this.putInt(0);
			return;
		}
		int size = array.length;
		checkArrayList(size);
		this.putInt(size);
		for (long i : array) {
			this.putLong(i);
		}
	}

	public void putLongList(Collection<Long> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (long i : list) {
			this.putLong(i);
		}
	}

	public void putStringList(Collection<String> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (String str : list) {
			this.putString(str);
		}
	}

	public void putStringArray(String[] array) {
		if (array == null || array.length == 0) {
			this.putInt(0);
			return;
		}
		int size = array.length;
		checkArrayList(size);
		this.putInt(size);
		for (String str : array) {
			this.putString(str);
		}
	}

	public void putFloatList(Collection<Float> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (float f : list) {
			this.putFloat(f);
		}
	}

	public void putDoubleList(Collection<Double> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (double d : list) {
			this.putDouble(d);
		}
	}

	public void putInt(int i) {
		this.checkBuffer(4);
		this.buffer.putInt(i);
	}

	public int getInt() {
		return this.buffer.getInt();
	}

	public void putShort(short s) {
		this.checkBuffer(2);
		this.buffer.putShort(s);
	}

	public short getShort() {
		return this.buffer.getShort();
	}

	public void putLong(long l) {
		this.checkBuffer(8);
		this.buffer.putLong(l);
	}

	public long getLong() {
		return this.buffer.getLong();
	}

	public void putFloat(float f) {
		this.checkBuffer(4);
		this.buffer.putFloat(f);
	}

	public float getFloat() {
		return this.buffer.getFloat();
	}

	public void putDouble(double d) {
		this.checkBuffer(8);
		this.buffer.putDouble(d);
	}

	public double getDouble() {
		return this.buffer.getDouble();
	}

	public void putByte(byte b) {
		this.checkBuffer(1);
		this.buffer.put(b);
	}

	public byte getByte() {
		return this.buffer.get();
	}

	public byte[] getByteArray() {
		int size = this.buffer.getInt();
		checkArrayList(size);
		byte[] array = new byte[size];
		for (int index = 0; index < array.length; index++) {
			array[index] = getByte();
		}
		return array;
	}

	public void putByteList(Collection<Byte> list) {
		if (list == null || list.isEmpty()) {
			this.putInt(0);
			return;
		}
		int size = list.size();
		checkArrayList(size);
		this.putInt(size);
		for (byte i : list) {
			this.putByte(i);
		}
	}

	public void putByteArray(byte[] array) {
		if (array == null || array.length == 0) {
			this.putInt(0);
			return;
		}
		int size = array.length;
		checkArrayList(size);
		this.putInt(size);
		for (byte i : array) {
			this.putByte(i);
		}
	}

	public void putBoolean(boolean value) {
		if (value) {
			this.putByte((byte) 1);
		} else {
			this.putByte((byte) 0);
		}
	}

	public boolean getBoolean() {
		return this.getByte() == 0 ? false : true;
	}

	public void put(byte[] data) {
		this.checkBuffer(data.length);
		this.buffer.put(data);
	}

	public void get(byte[] dst) {
		this.buffer.get(dst);
	}

	public void get(byte[] dst, int offset, int length) {
		this.buffer.get(dst, offset, length);
	}

	public void putString(String str) {
		if (str == null || str.equals("")) {
			this.putInt(0);
			return;
		}
		byte[] data = str.getBytes(defaultCharset);
		int size = data.length;
		this.checkBuffer(4 + data.length);
		this.putInt(size);
		if (size > 0)
			this.put(data);
	}

	public String getString() {
		final int length = this.getInt();
		checkArrayList(length);
		if (length == 0) {
			return "";
		}
		byte[] str = new byte[length];
		this.get(str, 0, length);
		return new String(str, defaultCharset);
	}

	public <T extends Deserializable> T getObject(ConstructorAccess<T> constructorAccess) {
		boolean isNull = this.getBoolean();
		if (isNull)
			return null;
		else {
			T t = constructorAccess.newInstance();
			t.deserialize(this);
			return t;
		}
	}

	public <T extends Deserializable> List<T> getObjectList(ConstructorAccess<T> constructorAccess) {
		int size = this.getInt();
		checkArrayList(size);
		List<T> resultList = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			T t = this.getObject(constructorAccess);
			resultList.add(t);
		}
		return resultList;
	}

	public <T extends Deserializable> Map<Integer, T> getObjectMap(ConstructorAccess<T> constructorAccess) {
		int size = this.getInt();
		checkArrayList(size);
		Map<Integer, T> result = new HashMap<>(size);
		for (int i = 0; i < size; i++) {
			int key = this.getInt();
			T t = this.getObject(constructorAccess);
			result.put(key, t);
		}
		return result;
	}

	public List<Integer> getIntList() {
		int size = this.getInt();
		checkArrayList(size);
		List<Integer> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			int value = this.getInt();
			list.add(value);
		}
		return list;
	}

	public int[] getIntArray() {
		int size = this.getInt();
		checkArrayList(size);
		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			int value = this.getInt();
			array[i] = value;
		}
		return array;
	}

	public List<Long> getLongList() {
		int size = this.getInt();
		checkArrayList(size);
		List<Long> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			long value = this.getLong();
			list.add(value);
		}
		return list;
	}

	public long[] getLongArray() {
		int size = this.getInt();
		checkArrayList(size);
		long[] array = new long[size];
		for (int i = 0; i < size; i++) {
			long value = this.getLong();
			array[i] = value;
		}
		return array;
	}

	public List<Float> getFloatList() {
		int size = this.getInt();
		checkArrayList(size);
		List<Float> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			float value = this.getFloat();
			list.add(value);
		}
		return list;
	}

	public List<Double> getDoubleList() {
		int size = this.getInt();
		checkArrayList(size);
		List<Double> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			double value = this.getDouble();
			list.add(value);
		}
		return list;
	}

	public List<String> getStringList() {
		int size = this.getInt();
		checkArrayList(size);
		List<String> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			String value = this.getString();
			list.add(value);
		}
		return list;
	}

	public String[] getStringArray() {
		int size = this.getInt();
		checkArrayList(size);
		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			String value = this.getString();
			array[i] = value;
		}
		return array;
	}

	public static int getByteCount(String str) {
		if (str == null || str.equals("")) {
			return 4;
		}
		byte[] data = str.getBytes(defaultCharset);
		int size = data.length;
		return size + 4;
	}

	public static <T> int getByteCount(List<T> list) {
		if (list == null || list.isEmpty()) {
			return 4;
		}

		T t = list.get(0);
		if (t instanceof Integer) {
			return 4 + 4 * list.size();
		} else if (t instanceof Long) {
			return 4 + 8 * list.size();
		} else if (t instanceof Float) {
			return 4 + 4 * list.size();
		} else if (t instanceof Double) {
			return 4 + 8 * list.size();
		} else if (t instanceof Boolean) {
			return 4 + 1 * list.size();
		} else if (t instanceof String) {
			return 4 + getByteCount((String) t);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void checkBuffer(int needRemaining) {
		if (buffer.remaining() < needRemaining) {
			extendBuffer(needRemaining);
		}
	}

	private void extendBuffer(int needRemaining) {
		ByteBuffer tmp = buffer;
		int pos = tmp.position();
		tmp.position(0);

		int capacity = Math.max(buffer.capacity() * 2, needRemaining);
		buffer = ByteBuffer.allocate(capacity);
		buffer.order(AioTcpSession.BYTE_ORDER);
		buffer.put(tmp.array(), 0, pos);

		logger.debug("###################### extendBuffer -> ", new Throwable());
	}

	public int position() {
		return buffer.position();
	}

	public ByteBuffer buffer() {
		return this.buffer;
	}

	public byte[] getBytes() {
		this.flip();
		byte[] data = new byte[this.limit()];
		this.get(data);
		return data;
	}

	public byte[] getData() {
		int posistion = this.position();
		int limit = this.limit();
		byte[] data = new byte[limit];
		this.buffer.position(0);
		this.buffer.get(data, 0, limit);
		this.buffer.position(posistion);
		return data;
	}

	public byte[] getOriginalData() {
		int posistion = this.position();
		byte[] data = new byte[posistion];
		this.buffer.position(0);
		this.buffer.get(data, 0, posistion);
		this.buffer.position(posistion);
		return data;
	}

//	public static void main(String[] args) {
//		ByteBufferManager buffer = new ByteBufferManager(4);
//		buffer.putInt(1);
//		System.out.println(buffer.buffer.capacity());
//		buffer.putInt(4);
//		System.out.println(buffer.buffer.capacity());
//
//		buffer.flip();
//		System.out.println("->" + buffer.getInt());
//		System.out.println("->" + buffer.getInt());
//	}
}
