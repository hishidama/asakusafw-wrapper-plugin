package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import jp.hishidama.eclipse_plugin.jdt.hyperlink.CompilationUnitHyperlinkDetector;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class OpenDeclaredMasterSelectionHyperlinkDetector extends CompilationUnitHyperlinkDetector {

	@Override
	protected IHyperlink[] detectHyperlinks(ICompilationUnit unit, IRegion region) {
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
