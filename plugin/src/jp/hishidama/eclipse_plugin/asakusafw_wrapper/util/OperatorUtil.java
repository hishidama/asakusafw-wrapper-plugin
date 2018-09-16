package jp.hishidama.eclipse_plugin.asakusafw_wrapper.util;

import java.util.HashSet;
import java.util.Set;

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

	private static Set<String> USER_OPERATOR_NAME_SET = null;

	public static boolean isUserOperator(IMethod method) {
		if (USER_OPERATOR_NAME_SET == null) {
			Set<String> set = new HashSet<String>();
			for (OperatorType value : OperatorType.values()) {
				if (value.isUser()) {
					set.add(value.getTypeName());
				}
			}
			USER_OPERATOR_NAME_SET = set;
		}
		return AnnotationUtil.getAnnotation(method.getDeclaringType(), method, USER_OPERATOR_NAME_SET) != null;
	}

	public static boolean isMasterSelection(IMethod method) {
		return isOperator(method, OperatorType.MASTER_SELECTION);
	}
}
