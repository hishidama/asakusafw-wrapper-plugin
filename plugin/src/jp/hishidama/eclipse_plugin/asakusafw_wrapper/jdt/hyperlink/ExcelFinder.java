package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class ExcelFinder extends ASTVisitor {
	private ICompilationUnit unit;
	private int offset;

	private String methodName;
	private IRegion region;
	private String excelName;

	public ExcelFinder(ICompilationUnit unit, int offset) {
		this.unit = unit;
		this.offset = offset;
	}

	public String getMethodName() {
		visit();
		return methodName;
	}

	public String getExcelName() {
		visit();
		return excelName;
	}

	public IRegion getRegion() {
		visit();
		return region;
	}

	private boolean visited = false;

	private void visit() {
		if (visited) {
			return;
		}
		visited = true;

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(unit);
		parser.setSourceRange(offset, 1);
		ASTNode node = parser.createAST(new NullProgressMonitor());
		node.accept(this);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		int offset = node.getStartPosition();
		int length = node.getLength();
		return offset <= this.offset && this.offset <= offset + length;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		String name = node.getName().getIdentifier();
		if ("prepare".equals(name) || "verify".equals(name)) {
			this.methodName = name;
			return true;
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		String value = node.getLiteralValue();
		int n = value.indexOf(".xls");
		if (n < 0) {
			return false;
		}
		int e = value.indexOf('#', n);
		if (e < 0) {
			e = value.length();
		}

		String file = value.substring(0, e).trim();
		if (file.endsWith(".xls") || file.endsWith(".xlsx")) {
			Region r = new Region(node.getStartPosition() + 1, file.length());
			if (r.getOffset() <= offset && offset < r.getOffset() + r.getLength()) {
				this.excelName = file;
				this.region = r;
			}
		}
		return false;
	}
}
