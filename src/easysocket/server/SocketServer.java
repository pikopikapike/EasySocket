package easysocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import easysocket.packet.PacketBuilder;
import easysocket.packet.PacketWriter;
import easysocket.session.AioTcpSession;
import easysocket.utils.PrintStackTrace;

public class SocketServer {

	public static interface ClientConnectedEventHandler {
		public void OnConnect(AioTcpSession session);
	}

	private CompletionHandler<AsynchronousSocketChannel, Void> acceptCompletionHanlder =
			new CompletionHandler<AsynchronousSocketChannel, Void>() {

				@Override
				public void completed(AsynchronousSocketChannel channel, Void attachment) {
					SocketServer.this.pendingAccept();

					try {
						AioTcpSession session = new AioTcpSession(channel, maxPacketSize);
						if (shouldEncryptPacket) {
							PacketBuilder packetBuilder = session.getPacketBuilder();

							byte[] keyBytes = new byte[16];
							for (int index = 0; index < keyBytes.length; index++) {
								keyBytes[index] = (byte) (ThreadLocalRandom.current().nextInt(Byte.MAX_VALUE));
							}

							// FIXME 추가작인 난독화 필요한듯
							PacketWriter packetWriter = PacketWriter.init(Integer.MAX_VALUE);
							packetWriter.putBoolean(shouldEncryptPacket);
							packetWriter.putByteArray(keyBytes);
							session.sendPacket(packetWriter);

							packetBuilder.enableDecryptor(keyBytes);
						} else {
							PacketWriter packetWriter = PacketWriter.init(Integer.MAX_VALUE);
							packetWriter.putBoolean(shouldEncryptPacket);
							session.sendPacket(packetWriter);
						}

						int sessionId = session.getSessionId();
						SESSION_POOL.put(sessionId, session);

						if (connectedEventHandler != null) {
							connectedEventHandler.OnConnect(session);
						}

						session.pendingRead();

					} catch (Exception ex) {
						PrintStackTrace.print(logger, ex);
					}
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					PrintStackTrace.print(logger, exc);
				}
			};

	static Logger logger = LogManager.getLogger(SocketServer.class);
	private final Map<Integer, AioTcpSession> SESSION_POOL = new ConcurrentHashMap<>();

	private AsynchronousServerSocketChannel server;
	private final ClientConnectedEventHandler connectedEventHandler;
	private final int port;
	private final int maxPacketSize;
	private final boolean shouldEncryptPacket;

	public SocketServer(int port, ClientConnectedEventHandler connectedEventHandler, int maxPacketSize,
			boolean shouldEncryptPacket) {
		this.port = port;
		this.maxPacketSize = maxPacketSize;
		this.shouldEncryptPacket = shouldEncryptPacket;
		this.connectedEventHandler = connectedEventHandler;
	}

	public void start() throws IOException {
		int initialSize = Runtime.getRuntime().availableProcessors();
		AsynchronousChannelGroup channelGroup =
				AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), initialSize);
		server = AsynchronousServerSocketChannel.open(channelGroup);
		server.bind(new InetSocketAddress(port));

		this.pendingAccept();
		logger.debug("server started at port " + this.port);
	}

	private void pendingAccept() {
		if (this.server == null) {
			throw new NullPointerException("server is null, invoke start before pendingAccept");
		}

		this.server.accept(null, this.acceptCompletionHanlder);
	}

	public AioTcpSession findClient(int sessionId) {
		return this.SESSION_POOL.get(sessionId);
	}

	public void broadcast(PacketWriter packet) {
		Collection<AioTcpSession> sessions = SESSION_POOL.values();
		for (AioTcpSession session : sessions) {
			session.sendPacket(packet);
		}
	}

	public void stop() throws IOException {
		if (this.server == null) {
			throw new IllegalStateException("server never opend");
		}
		this.server.close();
	}
}
