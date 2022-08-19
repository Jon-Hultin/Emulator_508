package com.jagex.js5.util;

public final class StringUtils {

	public static int hash(String str) {
		int hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + ((hash << 5) - hash);
		}
		return hash;
	}

	private StringUtils() {
		/* empty */
	}

}
