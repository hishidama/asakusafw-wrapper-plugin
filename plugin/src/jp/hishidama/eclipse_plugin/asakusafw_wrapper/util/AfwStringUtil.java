package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

public class AfwStringUtil {

	public static String extractModelClassName(String text) {
		if (text == null) {
			return null;
		}

		int s = text.indexOf('<');
		if (s >= 0) {
			int e = text.indexOf('>', s + 1);
			if (e < 0) {
				e = text.length();
			}
			return text.substring(s + 1, e);
		}
		return text;
	}
}
