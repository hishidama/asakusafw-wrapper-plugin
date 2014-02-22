package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class MasterSelectionFinder extends ASTVisitor {
	private ICompilationUnit unit;
	private int offset;

	private IRegion region;
	private String selectionName;

	public MasterSelectionFinder(ICompilationUnit unit, int offset) {
		this.unit = unit;
		this.offset = offset;
	}

	public String getMasterSelectionName() {
		visit();
		return selectionName;
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
	public boolean visit(NormalAnnotation node) {
		String name = node.getTypeName().getFullyQualifiedName();
		return "MasterCheck".equals(name) || "MasterJoin".equals(name) || "MasterBranch".equals(name)
				|| "MasterJoinUpdate".equals(name);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		String name = node.getName().getIdentifier();
		return "selection".equals(name);
	}

	@Override
	public boolean visit(StringLiteral node) {
		String value = node.getLiteralValue();
		this.selectionName = value.trim();
		this.region = new Region(node.getStartPosition() + 1, node.getLength() - 2);
		return false;
	}
}
