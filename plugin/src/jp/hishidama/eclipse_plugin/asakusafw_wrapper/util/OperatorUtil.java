package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import jp.hishidama.eclipse_plugin.util.StringUtil;

public class OperatorUtil {

	public static String convertUnderscoreName(String name) {
		if (!name.contains("_")) {
			return name;
		}

		if (StringUtil.isAll(name, '_')) {
			return "_";
		}

		String[] parts = name.split("_");
		StringBuilder sb = new StringBuilder(name.length());
		for (String part : parts) {
			if (sb.length() == 0) {
				sb.append(part.toLowerCase());
			} else {
				sb.append(Character.toUpperCase(part.charAt(0)));
				sb.append(part.substring(1).toLowerCase());
			}
		}
		return sb.toString();
	}
}
