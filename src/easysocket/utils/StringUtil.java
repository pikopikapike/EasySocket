package easysocket.utils;

import java.nio.charset.Charset;

public class StringUtil {
	static final Charset defaultCharset = Charset.forName("UTF-8");

	public static int calculateStringDataLength(String str) {
		if (str == null || str.equals("")) {
			return 4;
		}
		int size = str.getBytes(defaultCharset).length;
		return size + 4;
	}
}
