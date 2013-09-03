package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import jp.hishidama.eclipse_plugin.jdt.util.AnnotationUtil;
import jp.hishidama.eclipse_plugin.jdt.util.TypeUtil;
import jp.hishidama.eclipse_plugin.util.StringUtil;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

// @see org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector
public class OpenDeclaredOperatorHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor editor = (ITextEditor) getAdapter(ITextEditor.class);
		if (editor == null) {
			return null;
		}
		int offset = region.getOffset();
		return detectHyperlinks(editor, offset);
	}

	public IHyperlink[] detectHyperlinks(ITextEditor editor, int offset) {
		IEditorInput input = editor.getEditorInput();
		IJavaElement element = (IJavaElement) input.getAdapter(IJavaElement.class);

		IDocument document = editor.getDocumentProvider().getDocument(input);
		IRegion word = findWord(document, offset);
		if (word == null || word.getLength() == 0) {
			return null;
		}
		IJavaElement[] codes;
		try {
			ITypeRoot root = (ITypeRoot) element.getAdapter(ITypeRoot.class);
			codes = root.codeSelect(word.getOffset(), word.getLength());
		} catch (JavaModelException e) {
			return null;
		}
		for (IJavaElement code : codes) {
			int elementType = code.getElementType();
			switch (elementType) {
			case IJavaElement.TYPE:
				IHyperlink[] tr = detectTypeHyperlinks((IType) code, word);
				if (tr != null) {
					return tr;
				}
				break;
			case IJavaElement.METHOD:
				String methodName = code.getElementName();
				IHyperlink[] mr = detectMethodHyperlinks(code, methodName, word);
				if (mr != null) {
					return mr;
				}
				break;
			}
		}
		return null;
	}

	private static IRegion findWord(IDocument document, int offset) {
		int start = -2;
		int end = -1;
		try {
			int pos;
			for (pos = offset; pos >= 0; pos--) {
				char c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}
			}

			start = pos;
			pos = offset;
			for (int length = document.getLength(); pos < length; pos++) {
				char c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}
			}

			end = pos;
		} catch (BadLocationException e) {
		}
		if (start >= -1 && end > -1) {
			if (start == offset && end == offset) {
				return new Region(offset, 0);
			}
			if (start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		} else {
			return null;
		}
	}

	private IHyperlink[] detectTypeHyperlinks(IType type, IRegion word) {
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

	private IHyperlink[] detectMethodHyperlinks(IJavaElement code, String name, IRegion word) {
		IType type = (IType) code.getParent();
		return detectMethodHyperlinks(type, name, word);
	}

	private IHyperlink[] detectMethodHyperlinks(IType type, String name, IRegion word) {
		IType operator = getOperator(type);
		if (operator == null) {
			return null;
		}
		try {
			for (IMethod method : operator.getMethods()) {
				if (method.getElementName().equals(name)) {
					return new IHyperlink[] { new DeclaredOperatorHyperlink(method, word) };
				}
			}
		} catch (JavaModelException e) {
			// fall through
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
}
