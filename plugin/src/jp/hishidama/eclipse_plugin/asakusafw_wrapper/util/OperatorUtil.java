package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.operator.OperatorType;
import jp.hishidama.eclipse_plugin.jdt.util.AnnotationUtil;
import jp.hishidama.eclipse_plugin.util.StringUtil;

import org.eclipse.jdt.core.IMethod;

public class OperatorUtil {

	public static final String KEY_NAME = "com.asakusafw.vocabulary.model.Key";

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

	public static boolean isOperator(IMethod method, OperatorType operator) {
		return AnnotationUtil.getAnnotation(method.getDeclaringType(), method, operator.getTypeName()) != null;
	}
}
