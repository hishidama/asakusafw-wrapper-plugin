package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.WorkingCopyManager;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class OpenDeclaredMasterSelectionHyperlinkDetector extends AbstractHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor editor = (ITextEditor) getAdapter(ITextEditor.class);
		if (editor == null) {
			return null;
		}
		ICompilationUnit unit = getCompilationUnit(editor);
		if (unit == null) {
			return null;
		}

		return detectHyperlinks(unit, region);
	}

	protected ICompilationUnit getCompilationUnit(ITextEditor editor) {
		IEditorInput input = editor.getEditorInput();
		WorkingCopyManager manager = JavaPlugin.getDefault().getWorkingCopyManager();
		return manager.getWorkingCopy(input, false);
	}

	private IHyperlink[] detectHyperlinks(ICompilationUnit unit, IRegion region) {
		MasterSelectionFinder finder = new MasterSelectionFinder(unit, region.getOffset());
		String selectionName = finder.getMasterSelectionName();
		if (selectionName != null) {
			IType type = unit.findPrimaryType();
			try {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					if (method.getElementName().equals(selectionName)) {
						return new IHyperlink[] { new DeclaredMasterSelectionHyperlink(method, finder.getRegion()) };
					}
				}
			} catch (JavaModelException e) {
				return null;
			}
		}

		return null;
	}
}
