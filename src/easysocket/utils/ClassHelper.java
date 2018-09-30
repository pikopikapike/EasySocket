package easysocket.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassHelper {

	/**
	 * ä»åŒ…packageä¸?·?–æ??‰çš„Class
	 * 
	 * @param pack
	 * @return
	 */
	public static Set<Class<?>> getClasses(String pack) {

		// ç¬¬ä?ä¸ªclassç±»çš„?†åˆ
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// ??¦å¾ªç¯è¿?»£
		boolean recursive = true;
		// ?·å–?…çš„?å­— å¹¶è¿›è¡Œæ›¿??
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// å®šä¹‰ä¸?¸ª?šä¸¾?„é›†??å¹¶è¿›è¡Œå¾ª??¥å¤„ç†è¿™ä¸ª??½•ä¸‹çš„things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader()
					.getResources(packageDirName);
			// å¾ªç¯è¿?»£ä¸‹å»
			while (dirs.hasMoreElements()) {
				// ?·å–ä¸‹ä?ä¸ªå…ƒç´?
				URL url = dirs.nextElement();
				// å¾—åˆ°?è??„åç§?
				String protocol = url.getProtocol();
				// å¦‚æœ??»¥?‡ä»¶?„å½¢å¼ä¿å­˜åœ¨?åŠ¡?¨ä¸Š
				if ("file".equals(protocol)) {
					// ?·å–?…çš„?©ç†è·?¾„
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// ä»¥æ–‡ä»¶çš„?¹å¼?«æ?´ä¸ª?…ä¸‹?„æ–‡ä»?å¹¶æ·»? åˆ°?†åˆä¸?
					findAndAddClassesInPackageByFile(packageName, filePath,
							recursive, classes);
				} else if ("jar".equals(protocol)) {
					// å¦‚æœ?¯jar?…æ–‡ä»?
					// å®šä¹‰ä¸?¸ªJarFile
					JarFile jar;
					try {
						// ?·å–jar
						jar = ((JarURLConnection) url.openConnection())
								.getJarFile();
						// ä»æ?jar??å¾—åˆ°ä¸?¸ª?šä¸¾ç±?
						Enumeration<JarEntry> entries = jar.entries();
						// ?Œæ ·?„è¿›è¡Œå¾ª??¿­ä»?
						while (entries.hasMoreElements()) {
							// ?·å–jar?Œçš„ä¸?¸ªå®ä½“ ??»¥??›®å½??Œä?äº›jar?…é‡Œ?„å…¶ä»–æ–‡ä»?å¦‚META-INFç­‰æ–‡ä»?
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// å¦‚æœ??»¥/å¼?¤´??
							if (name.charAt(0) == '/') {
								// ?·å–?é¢?„å­—ç¬¦ä¸²
								name = name.substring(1);
							}
							// å¦‚æœ?åŠ?¨åˆ†?Œå®šä¹‰çš„?…å?¸åŒ
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// å¦‚æœä»?/"ç»“å°¾ ???ä¸ªåŒ…
								if (idx != -1) {
									// ?·å–?…å ??/"?¿æ¢??."
									packageName = name.substring(0, idx)
											.replace('/', '.');
								}
								// å¦‚æœ??»¥è¿?»£ä¸‹å» å¹¶ä¸”???ä¸ªåŒ…
								if ((idx != -1) || recursive) {
									// å¦‚æœ???ä¸?class?‡ä»¶ ?Œä¸”ä¸æ˜¯??½•
									if (name.endsWith(".class")
											&& !entry.isDirectory()) {
										// ?»æ‰?é¢??.class" ?·å–?Ÿæ??„ç±»??
										String className = name.substring(
												packageName.length() + 1,
												name.length() - 6);
										try {
											// æ·»åŠ ?°classes
											classes.add(Class
													.forName(packageName + '.'
															+ className));
										} catch (ClassNotFoundException e) {
											// log
											// .error("æ·»åŠ ?¨æˆ·?ªå®šä¹‰è§†?¾ç±»?™è? ?¾ä¸?°æ?ç±»çš„.class?‡ä»¶");
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("?¨æ‰«?ç”¨?·å®šä¹‰è§†?¾æ—¶ä»jar?…è·?–æ–‡ä»¶å‡º??);
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * ä»¥æ–‡ä»¶çš„å½¢å¼?¥è·?–åŒ…ä¸‹çš„??œ‰Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName,
			String packagePath, final boolean recursive, Set<Class<?>> classes) {
		// ?·å–æ­¤åŒ…?„ç›®å½?å»ºç«‹ä¸?¸ªFile
		File dir = new File(packagePath);
		// å¦‚æœä¸å­˜?¨æˆ–??ä¹Ÿä¸??›®å½•å°±?´æ¥è¿”å›
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("?¨æˆ·å®šä¹‰?…å " + packageName + " ä¸‹æ²¡?‰ä»»ä½•æ–‡ä»?);
			return;
		}
		// å¦‚æœå­˜åœ¨ å°±è·?–åŒ…ä¸‹çš„??œ‰?‡ä»¶ ?…æ‹¬??½•
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// ?ªå®šä¹‰è¿‡æ»¤è§„??å¦‚æœ??»¥å¾ªç¯(?…å«å­ç›®å½? ?–åˆ™??»¥.classç»“å°¾?„æ–‡ä»?ç¼–è¯‘å¥½çš„javaç±»æ–‡ä»?
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});
		// å¾ªç¯??œ‰?‡ä»¶
		for (File file : dirfiles) {
			// å¦‚æœ??›®å½??™ç»§ç»?‰«??
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(
						packageName + "." + file.getName(),
						file.getAbsolutePath(), recursive, classes);
			} else {
				// å¦‚æœ?¯javaç±»æ–‡ä»??»æ‰?é¢??class ?ªç•™ä¸‹ç±»??
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					// æ·»åŠ ?°é›†?ˆä¸­??
					// classes.add(Class.forName(packageName + '.' +
					// className));
					// ç»è¿‡?å¤?Œå??„æ?’ï¼Œè¿™é‡Œ?¨forName?‰ä?äº›ä¸å¥½ï¼Œä¼šè§¦?‘static?¹æ³•ï¼Œæ²¡?‰ä½¿?¨classLoader?„loadå¹²å?
					classes.add(Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// log.error("æ·»åŠ ?¨æˆ·?ªå®šä¹‰è§†?¾ç±»?™è? ?¾ä¸?°æ?ç±»çš„.class?‡ä»¶");
					e.printStackTrace();
				}
			}
		}
	}

}
