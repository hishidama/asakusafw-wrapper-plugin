package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import jp.hishidama.eclipse_plugin.jdt.hyperlink.CompilationUnitHyperlinkDetector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class OpenExcelHyperlinkDetector extends CompilationUnitHyperlinkDetector {

	@Override
	protected IHyperlink[] detectHyperlinks(ICompilationUnit unit, IRegion region) {
		int offset = region.getOffset();
		ExcelFinder finder = new ExcelFinder(unit, offset);
		String excelName = finder.getExcelName();
		if (excelName != null) {
			IProject project = unit.getJavaProject().getProject();
			IType type = unit.findPrimaryType();
			IPackageFragment pack = type.getPackageFragment();
			IFile file = project.getFile(String.format("src/test/resources/%s/%s",
					pack.getElementName().replace('.', '/'), excelName));
			if (file.exists()) {
				return new IHyperlink[] { new FileHyperlink(file, finder.getRegion(), true) };
			}
		}

		return null;
	}
}
