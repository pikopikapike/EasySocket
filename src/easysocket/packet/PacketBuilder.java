package easysocket.packet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import easysocket.exception.PacketSizeOverMaxException;
import easysocket.session.AioTcpSession;
import easysocket.utils.PrintStackTrace;

public class PacketBuilder {

	static final Logger logger = LogManager.getLogger(PacketBuilder.class);

	private final int maxPacketSize;

	private boolean isReadyToDecrypt = false;
	private Cipher decryptor;
	private Cipher encryptor;

	public PacketBuilder(int maxPacketSize) {
		this.maxPacketSize = maxPacketSize;
	}

	public void enableDecryptor(byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		if (this.decryptor == null) {
			this.decryptor = Cipher.getInstance("AES/ECB/PKCS5Padding");
		}

		if (this.encryptor == null) {
			this.encryptor = Cipher.getInstance("AES/ECB/PKCS5Padding");
		}

		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		decryptor.init(Cipher.DECRYPT_MODE, keySpec);
		encryptor.init(Cipher.ENCRYPT_MODE, keySpec);
		isReadyToDecrypt = true;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException {

		
		byte[] keyBytes = new byte[16];
		for (int index = 0; index < keyBytes.length; index++) {
			keyBytes[index] = (byte) (ThreadLocalRandom.current().nextInt(Byte.MAX_VALUE));
		}

		System.out.println(Base64.getEncoder().encodeToString(keyBytes));
	}

	public static void outputByteArray(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < data.length; index++) {
			sb.append(data[index]);
			sb.append(", ");
		}
		System.out.println(sb.toString());
	}

	public byte[] encryptPacket(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
		return encryptor.doFinal(data);
	}

	public boolean isReadyToDecrypt() {
		return this.isReadyToDecrypt;
	}

	public final List<PacketReader> parse(ByteBuffer buffer, AioTcpSession session)
			throws IllegalBlockSizeException, BadPaddingException {
		List<PacketReader> packets = new ArrayList<>();

		boolean loop = true;
		int remaining = 0;

		while (loop) {
			remaining = buffer.remaining();

			if (remaining >= 5) {
				int len = buffer.getInt(buffer.position());
				if (len > maxPacketSize) {
					logger.error("PakcetBuilder.parse, len > maxPacketSize,  length:{}, data ->{}", len, buffer.array());
					throw new PacketSizeOverMaxException();
				}
				if (len < 5) {
					logger.error("PakcetBuilder.parse, len < 5,  length:{}, data ->{}", len, buffer.array());
					try {
						session.close();
					} catch (IOException e) {
						logger.error("PacketBuilder.parse, " + e.getMessage());
						PrintStackTrace.print(logger, e);
					}
					return Collections.emptyList();
				}
				if (remaining >= len) {
					buffer.getInt(); // to forward position
					byte encrypted = buffer.get();

					byte[] data = new byte[len - 5];

					buffer.get(data);

					if (encrypted > 0) {
						if (isReadyToDecrypt == false) {
							throw new RuntimeException("received encrypted packet but encryptor is not ready...");
						} else {
							data = decryptor.doFinal(data);
						}
					}

					PacketReader packet = new PacketReader(data, session);
					packets.add(packet);
				} else {
					loop = false;
				}
			} else {
				loop = false;
			}
		}

		buffer.compact();
		buffer.clear();
		buffer.position(remaining);

		return packets;
	}
}
