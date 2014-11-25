package jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hishidama.eclipse_plugin.asakusafw_wrapper.jdt.hyperlink.MasterSelectionFinder;
import jp.hishidama.eclipse_plugin.jdt.util.AnnotationUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class MasterSelectionStringCompletionProposalComputer implements IJavaCompletionProposalComputer {

	// @Override
	public void sessionStarted() {
	}

	// @Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (!(context instanceof JavaContentAssistInvocationContext)) {
			return Collections.emptyList();
		}
		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

		ICompilationUnit cu = javaContext.getCompilationUnit();
		int offset = context.getInvocationOffset();
		MasterSelectionFinder finder = new MasterSelectionFinder(cu, offset);
		if (finder.getMemberName() == null) {
			return Collections.emptyList();
		}

		String prefix = getPrefix(finder, offset);
		if (prefix == null) {
			return Collections.emptyList();
		}

		IRegion region = finder.getRegion();
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		for (String s : getMasterSelectionMethod(cu)) {
			if (s.startsWith(prefix)) {
				int start, len;
				if (prefix.isEmpty()) {
					start = offset;
					len = 0;
				} else {
					start = region.getOffset();
					len = region.getLength();
				}
				CompletionProposal proposal = new CompletionProposal(s, start, len, s.length());
				list.add(proposal);
			}
		}
		return list;
	}

	private String getPrefix(MasterSelectionFinder finder, int offset) {
		String name = finder.getMasterSelectionName();
		if (name == null) {
			return "";
		}
		IRegion region = finder.getRegion();
		if (region.getOffset() + region.getLength() < offset) {
			return null;
		}
		int len = offset - region.getOffset();
		if (len <= 0) {
			return "";
		}
		if (len > name.length()) {
			return name;
		}
		return name.substring(0, len);
	}

	static List<String> getMasterSelectionMethod(ICompilationUnit cu) {
		List<String> list = new ArrayList<String>();
		try {
			IType type = cu.findPrimaryType();
			for (IMethod method : type.getMethods()) {
				if (AnnotationUtil.getAnnotation(type, method, "com.asakusafw.vocabulary.operator.MasterSelection") != null) {
					list.add(method.getElementName());
				}
			}
		} catch (JavaModelException e) {
			// fall through
		}
		return list;
	}

	// @Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return null;
	}

	// @Override
	public String getErrorMessage() {
		return null;
	}

	// @Override
	public void sessionEnded() {
	}
}
