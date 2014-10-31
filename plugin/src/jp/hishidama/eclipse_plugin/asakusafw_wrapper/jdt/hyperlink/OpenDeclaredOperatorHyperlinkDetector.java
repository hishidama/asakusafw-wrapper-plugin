package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import jp.hishidama.eclipse_plugin.jdt.hyperlink.JdtHyperlinkDetector;
import jp.hishidama.eclipse_plugin.jdt.util.AnnotationUtil;
import jp.hishidama.eclipse_plugin.jdt.util.TypeUtil;
import jp.hishidama.eclipse_plugin.util.StringUtil;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

// @see org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector
public class OpenDeclaredOperatorHyperlinkDetector extends JdtHyperlinkDetector {

	@Override
	protected IHyperlink[] detectTypeHyperlinks(IType type, IRegion word) {
		IType operator = getOperator(type);
		if (operator == null) {
			return detectMemberTypeHyperlinks(type, word);
		}
		return new IHyperlink[] { new DeclaredOperatorHyperlink(operator, word) };
	}

	private IHyperlink[] detectMemberTypeHyperlinks(IType type, IRegion word) {
		IType parent = type.getDeclaringType();
		if (parent == null) {
			return null;
		}
		String name = StringUtil.toFirstLower(type.getElementName());
		return detectMethodHyperlinks(parent, name, word);
	}

	@Override
	protected IHyperlink[] detectMethodHyperlinks(IMethod method, IRegion word) {
		IType type = (IType) method.getParent();
		return detectMethodHyperlinks(type, method.getElementName(), word);
	}

	private IHyperlink[] detectMethodHyperlinks(IType type, String name, IRegion word) {
		IType operator = getOperator(type);
		if (operator == null) {
			return null;
		}
		IMethod constructor = null;
		try {
			for (IMethod method : operator.getMethods()) {
				if (method.getElementName().equals(name)) {
					return new IHyperlink[] { new DeclaredOperatorHyperlink(method, word) };
				}
			}
			for (IMethod method : operator.getMethods()) {
				if (convertUnderscoreName(method.getElementName()).equals(name)) {
					return new IHyperlink[] { new DeclaredOperatorHyperlink(method, word) };
				}
				if (method.isConstructor()) {
					if (constructor == null) {
						constructor = method;
					}
				}
			}
		} catch (JavaModelException e) {
			// fall through
		}
		if (constructor != null) {
			return new IHyperlink[] { new DeclaredOperatorHyperlink(constructor, word) };
		}
		return new IHyperlink[] { new DeclaredOperatorHyperlink(operator, word) };
	}

	private IType getOperator(IType type) {
		String value = AnnotationUtil.getAnnotationValue(type, "com.asakusafw.vocabulary.operator.OperatorFactory",
				"value");
		if (value == null) {
			return null;
		}
		return TypeUtil.resolveType(value, type);
	}

	private String convertUnderscoreName(String name) {
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
