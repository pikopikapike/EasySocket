package easysocket.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringUtil {
	static final Charset defaultCharset = StandardCharsets.UTF_8;

	public static int calculateStringDataLength(String str) {
		if (str == null || str.equals("")) {
			return 4;
		}
		int size = str.getBytes(defaultCharset).length;
		return size + 4;
	}
}
