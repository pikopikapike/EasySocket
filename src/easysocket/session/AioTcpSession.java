package easysocket.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import easysocket.packet.PacketBuilder;
import easysocket.packet.PacketFrequencyChecker;
import easysocket.packet.PacketReader;
import easysocket.packet.PacketWriter;
import easysocket.session.event.SessionEventListener;
import easysocket.session.event.SessionReceivedPacketListener;
import easysocket.utils.PrintStackTrace;

/**
 * Use <code>AioTcpSession</code> to store the information of a socket
 * connection
 * 
 * Invoke {@link #onReceivePacket(SessionReceivedPacketListener)} to specify
 * packet received event lister
 * 
 * @see SessionReceivedPacketListener
 */
public class AioTcpSession {

	static final Logger logger = LogManager.getLogger(AioTcpSession.class);

	private class WriteCompletionHandler implements CompletionHandler<Integer, AioTcpSession> {
		@Override
		public void completed(Integer result, AioTcpSession session) {
			if (result < 0 || !session.getChannel().isOpen()) {
				try {
					close();
				} catch (IOException e) {
					logger.error("WriteCompletionHandler.completed, msg:" + e.getMessage());
					PrintStackTrace.print(logger, e);
				}
				return;
			}
			if (writeBuffer != null) {
				writeBuffer.position(result);
			}
			isWriting(false);
			write();
		}

		@Override
		public void failed(Throwable exc, AioTcpSession session) {
			try {
				close();
			} catch (IOException e) {
				logger.error("WriteCompletionHandler.failed, msg:" + e.getMessage());
				PrintStackTrace.print(logger, e);
			}
		}
	}

	private class ReadComletionHandler implements CompletionHandler<Integer, AioTcpSession> {
		@Override
		public void completed(Integer result, AioTcpSession session) {
			try {
				if (result > 0) {
					session.pendingRead();
				} else {
					session.close();
				}
			} catch (IOException e) {
				logger.error("ReadComletionHandler.completed. msg:" + e.getMessage());
				PrintStackTrace.print(logger, e);
				try {
					session.close();
				} catch (IOException e1) {
					logger.error("ReadComletionHandler.completed. msg:" + e1.getMessage());
					PrintStackTrace.print(logger, e1);
				}
			} catch (Exception e) {
				logger.error("ReadComletionHandler.completed. msg:" + e.getMessage());
				PrintStackTrace.print(logger, e);
				try {
					session.close();
				} catch (IOException e1) {
					logger.error("ReadComletionHandler.completed. msg:" + e.getMessage());
					PrintStackTrace.print(logger, e);
				}
			}
		}

		@Override
		public void failed(Throwable exc, AioTcpSession session) {
			try {
				session.close();
			} catch (IOException e) {
				logger.error("ReadComletionHandler.failed. msg:" + e.getMessage());
				PrintStackTrace.print(logger, e);
			} catch (Exception e) {
				logger.error("ReadComletionHandler.failed. msg:" + e.getMessage());
				PrintStackTrace.print(logger, e);
				try {
					session.close();
				} catch (IOException e1) {
					logger.error("ReadComletionHandler.failed. msg:" + e.getMessage());
					PrintStackTrace.print(logger, e);
				}
			}
		}
	}

	private ByteBuffer readBuffer = ByteBuffer.allocate(256);
	protected AsynchronousSocketChannel channel;
	protected AtomicBoolean isWriting = new AtomicBoolean(false);
	protected final int sessionId;
	protected CompletionHandler<Integer, AioTcpSession> readCompletionHandler;
	protected CompletionHandler<Integer, AioTcpSession> writeCompletionHandler;
	private final ConcurrentLinkedQueue<ByteBuffer> outs = new ConcurrentLinkedQueue<>();
	private ByteBuffer writeBuffer;
	protected static final AtomicInteger sessionIndex = new AtomicInteger(1);
	public final SessionState sessionState = new SessionState(SessionState.UNKNOWN);
	private List<SessionEventListener> eventListeners = new CopyOnWriteArrayList<>();
	public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	private AtomicBoolean isClosed = new AtomicBoolean(false);
	private long userId;

	private PacketBuilder packetBuilder;// = new PacketBuilder();
	private boolean checkPacketFrequency = true;
	private PacketFrequencyChecker frequencyChecker;
	private String remoteAddress;
	private int remotePort;

	public void registerEventListener(SessionEventListener listener) {
		this.eventListeners.add(listener);
	}

	public void unregisterEventListener(SessionEventListener listener) {
		this.eventListeners.remove(listener);
	}

	public void onReceivePacket(SessionReceivedPacketListener listener) {
		this.eventListeners.add(listener);
	}

	public AioTcpSession(AsynchronousSocketChannel channel, int maxPacketSize) throws IOException {
		this(channel, new PacketBuilder(maxPacketSize));
	}

	public AioTcpSession(AsynchronousSocketChannel channel, PacketBuilder packetBuilder) throws IOException {
		this.sessionId = sessionIndex.addAndGet(1);
		this.readCompletionHandler = new ReadComletionHandler();
		this.writeCompletionHandler = new WriteCompletionHandler();
		this.setChannel(channel);
		this.sessionState.set(SessionState.OPENED);
		this.readBuffer.order(BYTE_ORDER);
		this.packetBuilder = packetBuilder;
		this.frequencyChecker = new PacketFrequencyChecker(new Runnable() {
			
			@Override
			public void run() {
				onPacketFrequencyError();				
			}
		});
	}

	private void onPacketFrequencyError() {
		String remoteHost = "";
		try {
			remoteHost = this.getChannel().getRemoteAddress().toString();
		} catch (IOException e) {
			PrintStackTrace.print(logger, e);
		}
		try {
			this.close();
		} catch (IOException e) {
			PrintStackTrace.print(logger, e);
		}

		logger.error("AioTcpSession.onPacketFrequencyError, sessionId:{}, remoteHost:{}", this.sessionId, remoteHost);
	}

	protected void queuePacket(ByteBuffer buffer) {
		List<PacketReader> packets;
		try {
			packets = packetBuilder.parse(buffer, this);
			if (packets == null || packets.size() <= 0) {
				return;
			}
			this.onReceivedPackets(packets);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			PrintStackTrace.print(logger, e);
		}

	}

	private void onReceivedPackets(List<PacketReader> packets) {
		if (checkPacketFrequency && this.frequencyChecker.increaseReceiveCount(packets.size()) == false) {
			return;
		}

		PacketReader keyPacket = null;
		for (PacketReader packet : packets) {
			if (packet.getCmd() == Integer.MAX_VALUE) {
				if (packetBuilder.isReadyToDecrypt()) {
					logger.error("duplicate encryption key packet, close session");
					try {
						close();
					} catch (IOException e) {
						PrintStackTrace.print(logger, e);
					}
					break;
				}
				keyPacket = packet;
				boolean shouldEncrypt = packet.getBoolean();
				if (shouldEncrypt) {
					byte[] keyBytes = packet.getByteArray();
					try {
						packetBuilder.enableDecryptor(keyBytes);
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
						PrintStackTrace.print(logger, e);
					}
				}
			}
		}
		packets.remove(keyPacket);
		if (packets.isEmpty() == false) {
			for (SessionEventListener listener : this.eventListeners) {
				listener.onReceivePackets(packets);
			}
		}
	}

	private void beforeRead(ByteBuffer readBuffer) {
		readBuffer.flip();
		if (readBuffer.hasRemaining()) {
			this.queuePacket(readBuffer);
			// Check readBuffer is full
			if (readBuffer.position() > 4 && readBuffer.getInt(0) > readBuffer.capacity()) {
				extendReadBuffer();
			}
		} else {
			readBuffer.clear();
		}
	}

	public final void pendingRead() {
		beforeRead(this.readBuffer);
		// if (this.channel.isOpen())
		this.channel.read(this.readBuffer, this, this.readCompletionHandler);
	}

	private final void extendReadBuffer() {
		ByteBuffer tmp = readBuffer;
		int pos = tmp.position();
		tmp.position(0);

		readBuffer = ByteBuffer.allocate(readBuffer.capacity() * 2);
		readBuffer.order(BYTE_ORDER);
		readBuffer.put(tmp.array(), 0, pos);
	}

	public void close() throws IOException {
		if (isClosed.compareAndSet(false, true)) {
			this.onClose();
			if (this.channel.isOpen())
				this.channel.close();
		}
	}

	private void onClose() {
		for (SessionEventListener listener : this.eventListeners) {
			listener.onClose();
		}
	}

	public boolean isClosed() {
		return this.isClosed.get();
	}

	protected final void pushWriteData(ByteBuffer buffer) {
		outs.add(buffer);
		write();
	}

	public void sendPacket(PacketWriter packet) {

		byte[] data = packet.getBytes();
		boolean shouldEncrypted = packetBuilder.isReadyToDecrypt();
		sendEncrypedPacket(shouldEncrypted, data);

	}

	public void sendEncrypedPacket(boolean shouldEncrypted, byte[] data) {
		try {
			if (shouldEncrypted) {
				data = packetBuilder.encryptPacket(data);
			}
			sendPacket(shouldEncrypted, data);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			PrintStackTrace.print(logger, e);
		}
	}

	public void sendPacket(boolean isEncrypted, byte[] data) {
		ByteBuffer buffer = this.formatSendPacket(isEncrypted, data);
		this.pushWriteData(buffer);
	}

	private ByteBuffer formatSendPacket(boolean isEncrypted, byte[] data) {

		byte encrypted = (byte) (isEncrypted ? 1 : 0);

		int packetSize = 4 + 1 + data.length;

		ByteBuffer buffer = ByteBuffer.allocate(packetSize);
		buffer.order(BYTE_ORDER);
		buffer.putInt(packetSize);
		buffer.put(encrypted);
		buffer.put(data);
		buffer.flip();

		return buffer;
	}

	private final void write() {
		if (isWriting.compareAndSet(false, true)) {
			if (this.writeBuffer != null) {
				int pos = this.writeBuffer.position();
				int limit = this.writeBuffer.limit();
				if (pos >= limit) {
					this.writeBuffer = null;
				} else {
					this.writeBuffer.compact();
					this.writeBuffer.flip();
					this.channel.write(this.writeBuffer, this, this.writeCompletionHandler);
					return;
				}
			}
			ByteBuffer buffer = outs.poll();
			if (buffer != null) {
				this.writeBuffer = buffer;
				this.channel.write(buffer, this, this.writeCompletionHandler);
			} else {
				isWriting.set(false);
			}
		}
	}

	public final boolean isWriting() {
		return this.isWriting.get();
	}

	public final void isWriting(boolean isWriting) {
		this.isWriting.set(isWriting);
	}

	public final boolean isChannelOpen() {
		return channel.isOpen();
	}

	public final AsynchronousSocketChannel getChannel() {
		return channel;
	}

	public final void setChannel(AsynchronousSocketChannel channel) throws IOException {
		this.channel = channel;

		InetSocketAddress sa = (InetSocketAddress) this.channel.getRemoteAddress();
		String addr = sa.getAddress().getHostAddress();
		this.remoteAddress = addr;
		this.remotePort = sa.getPort();
	}

	public final int getSessionId() {
		return sessionId;
	}

	public final String remoteAddress() {
		return this.remoteAddress;
	}

	public final int remotePort() {
		return this.remotePort;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public boolean isCheckPacketFrequency() {
		return checkPacketFrequency;
	}

	public void setCheckPacketFrequency(boolean checkPacketFrequency) {
		this.checkPacketFrequency = checkPacketFrequency;
	}

	public PacketBuilder getPacketBuilder() {
		return this.packetBuilder;
	}
}
