package org.tomac.tools.converter.fixprotocol;

public class FixProtocolUtils {
	private static String version = "FIXT.1.1";
	
	public static void setVersion(String v) {
		version = v;
	}
	
	public static boolean isTagWithLength(String tag) {
		// Tag		Max Length
		if (getTagLength(tag).length()>0) return true;
		return false;
	}

	public static String getTagLength(String tag) {
		// Session
		if (tag.equals("8")) return String.valueOf(version.length());
		if (tag.equals("35")) return "2";
		if (tag.equals("9")) return "3";
		if (tag.equals("10")) return "3";
		return "";
	}
}
